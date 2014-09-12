package models.jenkins

import java.io.File

import components.{BranchRepositoryComponent, BuildRepositoryComponent, JenkinsServiceComponent, LoggedUserProviderComponent}
import models.{Branch, Build, IBuildInfo}
import play.api.Play
import play.api.Play.current

import scala.util.Try
import scalaj.http.Http


trait JenkinsServiceComponentImpl extends JenkinsServiceComponent {
  this: JenkinsServiceComponentImpl
    with BranchRepositoryComponent
    with BuildRepositoryComponent
    with LoggedUserProviderComponent
  =>

  val jenkinsService: JenkinsService = new JenkinsServiceImpl

  class JenkinsServiceImpl extends JenkinsService with FileApi with ParseFolder {
    private val jenkinsUrl = Play.configuration.getString("jenkins.url").get

    override def getUpdatedBuilds(existingBuilds: List[IBuildInfo], buildNamesToUpdate: Seq[String]): List[Build] = {
      val existingBuildsMap: Map[String, IBuildInfo] = existingBuilds.map(x => (x.name, x)).toMap

      val allFolders = new Folder(directory).listFiles().filter(_.isDirectory).toList

      val newFolders: List[Folder] = allFolders.filterNot(x => existingBuildsMap.get(x.getName).isDefined)

      val foldersToUpdate: List[Folder] = existingBuilds.filter(_.status.isEmpty).map(x => new Folder(directory, x.name))

      val folders = newFolders ++ foldersToUpdate ++ buildNamesToUpdate.toList.map(x => new Folder(directory, x))

      val buildSources: List[BuildSource] = folders.distinct.flatMap(createBuildSource)

      val result: List[Build] = buildSources.flatMap(buildSource => {
        val name: String = buildSource.folder.getName
        val toggled = existingBuildsMap.get(name).fold(false)(_.toggled)
        getBuild(buildSource, toggled)
      })

      result
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

    def getTestRun(branch: Branch, build: Int, part: String, run: String) = findBuild(branch, build)
      .map(b => b.getTestRunBuildNode(part, run))
      .flatten
      .map(testRunBuildNode => testRunBuildNode.copy(testResults = getTestCasePackages(testRunBuildNode)))

    def forceBuild(action: models.BuildAction) = Try {
      val url = s"$jenkinsUrl/job/$rootJobName/buildWithParameters"

      val parameters = action.parameters ++ loggedUser.map("WHO_STARTS" -> _.fullName)

      play.Logger.info(s"Force build to $url with parameters $parameters")

      Http.post(url)
        .params(parameters)
        .asString
    }

    def forceBuildCategory(maybeBuildParams: Option[BuildParams], maybeRevision: Option[String], filter: String, url: String, buildNumber: Int, buildPathPostfix: String) = {
      val params = for {
        revision <- maybeRevision.toList
        buildParams <- maybeBuildParams.toList
        param <- buildParams.parameters.map {
          case ("Cycle", value) => ("CYCLE", value)
          case ("BUILDPATH", value) => ("BUILDPATH", value + "\\" + buildPathPostfix)
          case ("LOCAL_BRANCH", value) => ("LOCALREPONAME", value)
          case ("ARTIFACTS", value) => ("ARTIFACTS", value)
          case (_, _) => ("", "")
        }
          .filter(x => x._1 != "")
          .++(List(
          ("VERSION", revision + "." + buildNumber),
          ("BUILDPRIORITY", "10")
        ))
      } yield param

      val paramsWithFilter = if (filter != "") {
        params.++(List(("FILTER", filter), ("RERUN", "true")))
      } else params

      play.Logger.info(s"Force build to $url with parameters $paramsWithFilter")

      Http.post(url)
        .params(paramsWithFilter.toList)
        .asString
    }

    def forceReuseArtifactsBuild(action: models.BranchWithArtifactsReuseCustomBuildAction) = Try {

      //This is Jenkins logic for naming artifacts folders
      val branchArtifactsFolderName = action.branch.replace("feature/", "").replace("merge/", "").replace("origin/", "")

      val buildFolder = new Folder(s"$directory/${branchArtifactsFolderName}_${action.buildNumber}")
      val maybeRevision = read(new File(buildFolder, "Artifacts/Revision.txt")).map(x => x.replaceAll("REVISION=", ""))

      val maybeBuildParams = BuildParams(getParamsFile(buildFolder))

      val funcTestsFilter = action.cycle.parameters.find(x => x.name == models.Cycle.funcTestsCategoryName).map(x => x.parts.mkString(" ")).getOrElse("")
      if (funcTestsFilter != "") {
        val funcTestsUrl = s"$jenkinsUrl/job/RunFuncTests/buildWithParameters"
        forceBuildCategory(maybeBuildParams, maybeRevision, funcTestsFilter, funcTestsUrl, action.buildNumber, "FuncTests")
      }

      val unitTestsFilter = action.cycle.parameters.find(x => x.name == models.Cycle.unitTestsCategoryName).map(x => x.parts.mkString(" ")).getOrElse("")
      if (unitTestsFilter != "") {
        val unitTestsUrl = s"$jenkinsUrl/job/RunUnitTests/buildWithParameters"
        forceBuildCategory(maybeBuildParams, maybeRevision, unitTestsFilter, unitTestsUrl, action.buildNumber, "UnitTests")
      }

      if (action.cycle.includeCasper){
        forceBuildCategory(maybeBuildParams, maybeRevision, "", s"$jenkinsUrl/job/RunCasperJSTests/buildWithParameters", action.buildNumber, "FuncTests")
      }

      if (action.cycle.includeComet){
        forceBuildCategory(maybeBuildParams, maybeRevision, "", s"$jenkinsUrl/job/CometOutOfProcess/buildWithParameters", action.buildNumber, "FuncTests")
      }

      if (action.cycle.includeSlice){
        forceBuildCategory(maybeBuildParams, maybeRevision, "", s"$jenkinsUrl/job/RunSliceLoadTest/buildWithParameters", action.buildNumber, "FuncTests")
      }

      if (action.cycle.includeDb){
        forceBuildCategory(maybeBuildParams, maybeRevision, "", s"$jenkinsUrl/job/RunDBTest/buildWithParameters", action.buildNumber, "FuncTests")
      }

      ""
    }
  }

}