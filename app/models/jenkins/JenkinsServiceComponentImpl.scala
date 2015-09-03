package models.jenkins

import java.io.File

import components._
import models.buildActions.{DeployBuildAction, JenkinsBuildAction, ReuseArtifactsBuildAction}
import models.{Branch, Build}
import src.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import scalaj.http.{Http, HttpOptions}

trait JenkinsServiceComponentImpl extends JenkinsServiceComponent {
  this: JenkinsServiceComponentImpl
    with BranchRepositoryComponent
    with BuildRepositoryComponent
    with LoggedUserProviderComponent
    with NotificationComponent
    with ConfigComponent
  =>

  val jenkinsService: JenkinsService = new JenkinsServiceImpl

  class JenkinsServiceImpl extends JenkinsService with ParseFolder {

    lazy val directory = config.jenkinsDataPath
    lazy val deployDirectory = config.deployDirectoryRoot

    lazy val unstableNodeNames = config.unstableNodes

    override def getUpdatedBuilds(existingBuilds: List[Build], buildNamesToUpdate: Seq[String]): List[Build] = {

      val existingBuildsMap: Map[String, Build] = existingBuilds.map(x => (x.name, x)).toMap


      val folders = if (buildNamesToUpdate.nonEmpty) {
        buildNamesToUpdate.map(x => new Folder(directory, x))
      }
      else {
        val allFolders = new Folder(directory).listFiles().filter(_.isDirectory).toList
        val newFolders: List[Folder] = allFolders.filterNot(x => existingBuildsMap.get(x.getName).isDefined)
        val foldersToUpdate: List[Folder] = existingBuilds.filter(_.status.isEmpty).map(x => new Folder(directory, x.name))
        newFolders ++ foldersToUpdate
      }

      val buildSources = folders.distinct.flatMap(createBuildSource)

      val result = buildSources.flatMap(buildSource => {
        val name: String = buildSource.folder.getName
        val toggled = existingBuildsMap.get(name).fold(false)(_.toggled)
        getBuild(buildSource, toggled)
      })

      result.toList
    }

    def getTestRun(branch: Branch, build: Int, part: String, run: String) =
      findBuild(branch, build)
        .flatMap(b => b.getTestRunBuildNode(part, run))
        .map(testRunBuildNode => testRunBuildNode.copy(testResults = getTestCasePackages(testRunBuildNode)))

    def forceBuild(action: JenkinsBuildAction) = action match {
      case x: ReuseArtifactsBuildAction => forceReuseArtifactsBuild(x)
      case x: JenkinsBuildAction => forceSimpleBuild(x)
    }

    def deployBuild(buildName: String, deployFolderName: String) = {


      val buildFolderPath: String = s"$directory\\$buildName\\Artifacts\\Code\\Releases\\"
      val deployFolderPath: String = s"$deployDirectory\\$deployFolderName\\"

      val buildFolder = new Folder(buildFolderPath)
      val deployFolder = new Folder(deployFolderPath)

      Future {
        Utils.watch(s"Deploy $buildFolderPath to $deployFolderPath") {
          for (file <- deployFolder.listFiles()) {
            file.delete()
          }

          for (file <- buildFolder.listFiles()) {
            if (file.getName.endsWith("archive.zip")) {
              FileApi.copyFile(file, deployFolder)
            }
          }
        }
      }
    }

    def canDeployBuild(buildName: String) = {
      val buildFolder = new Folder(s"$directory/$buildName/Artifacts/Code/Releases")
      buildFolder.exists
    }

    override def getBuildActions(build: Build) = {

      val deployActions = if (canDeployBuild(build.name))
        config.teams.map(team => DeployBuildAction(build.name, build.number, team.name))
      else
        Nil

      ReuseArtifactsBuildAction(build.name, build.number) :: deployActions
    }


    def getBuildNumbers(name: String): Option[(Int, Option[Int])] = {
      val prR = """pr_(\d+)_(\d+)""".r
      val buildR = """.*_(\d+)$""".r
      name match {
        case prR(prID, build) => Some((build.toInt, Some(prID.toInt)))
        case buildR(build) => Some((build.toInt, None))
        case _ => None
      }
    }

    def createBuildSource(folder: Folder): Option[BuildSource] = {
      val paramsFile = getParamsFile(folder)

      for {
        buildParams <- BuildParams(paramsFile)
        (buildNumber, prId) <- getBuildNumbers(folder.getName)
        branch <- prId.fold(branchRepository.getBranch(buildParams.branch.substring(7)))(f = id => branchRepository.getBranchByPullRequest(id))

      } yield BuildSource(branch.name, buildNumber, prId, folder, buildParams)
    }

    def getParamsFile(folder: Folder): File = {
      new File(folder, "Build/StartBuild/StartBuild.params")
    }

    def findBuild(branch: Branch, buildId: Int): Option[Build] = buildRepository.getBuild(branch, buildId)

    def post(url: String, parameters: List[(String, String)]) = Try {

      play.Logger.info(s"Force build to $url with parameters $parameters")


      Http.post(url)
        .params(parameters)
        .option(HttpOptions.connTimeout(1000))
        .option(HttpOptions.readTimeout(5000))
        .asString
    }

    def forceSimpleBuild(action: JenkinsBuildAction) = {
      val url = s"${config.jenkinsUrl}/job/$rootJobName/buildWithParameters"
      val parameters = action.parameters ++ loggedUser.map("WHO_STARTS" -> _.fullName) ++ List("UID" -> action.name)
      post(url, parameters)

    }

    def forceReuseArtifactsBuild(action: ReuseArtifactsBuildAction): Try[Unit] = Try {
      val buildFolder = new Folder(s"$directory/${action.buildName}")
      val revision = FileApi.readAsMap(new File(buildFolder, "Artifacts/Revision.txt"))
        .flatMap(
          _.values
            .filter(_.toString.startsWith("REVISION="))
            .map(_.replaceAll("REVISION=", ""))
            .headOption
        )
        .get

      val buildParams = BuildParams(getParamsFile(buildFolder)).get



      val forcePart = (job: String, postfix: String, filter: String) =>
        forceBuildCategory(buildParams, revision, filter.replaceAll( """^\"|\"$""", ""), s"${config.jenkinsUrl}/job/$job/buildWithParameters", action.buildNumber, postfix)

      if (action.cycle.funcTests != "") {
        forcePart("RunFuncTests", "FuncTests", action.cycle.funcTests)
      }

      if (action.cycle.unitTests != "") {
        forcePart(s"RunUnitTests", "UnitTests", action.cycle.unitTests)
      }

      if (action.cycle.includeCasper) {
        forcePart("RunCasperJSTests", "FuncTests", "")
      }

      if (action.cycle.includeComet) {
        forcePart("CometOutOfProcess", "FuncTests", "")
      }

      if (action.cycle.includeSlice) {
        forcePart("RunSliceLoadTest", "FuncTests", "")
      }

      if (action.cycle.includeDb) {
        forcePart("RunDBTest", "FuncTests", "")
      }
    }

    def forceBuildCategory(buildParams: BuildParams, revision: String, filter: String, url: String, buildNumber: Int, buildPathPostfix: String) = {

      val params: List[(String, String)] = buildParams.parameters.flatMap {
        case ("Cycle", value) => Some("CYCLE", value)
        case ("BUILDPATH", value) => Some("BUILDPATH", value + "\\" + buildPathPostfix)
        case ("LOCAL_BRANCH", value) => Some("LOCALREPONAME", value)
        case ("ARTIFACTS", value) => Some("ARTIFACTS", value)
        case _ => None
      }.toList ++
        List(
          ("VERSION", revision + "." + buildNumber),
          ("BUILDPRIORITY", "10")
        )

      val paramsWithFilter = if (filter != "") {
        params ++ List(("FILTER", filter), ("RERUN", "true"))
      } else params

      post(url, paramsWithFilter)
    }

  }

}