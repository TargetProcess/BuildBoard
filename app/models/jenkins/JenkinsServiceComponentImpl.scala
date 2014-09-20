package models.jenkins

import components.{LoggedUserProviderComponent, BuildRepositoryComponent, BranchRepositoryComponent, JenkinsServiceComponent}
import models.{Branch, Build}
import java.io.File
import play.api.Play
import scala.util.Try
import scalaj.http.Http
import play.api.Play.current


trait JenkinsServiceComponentImpl extends JenkinsServiceComponent {
  this: JenkinsServiceComponentImpl
    with BranchRepositoryComponent
    with BuildRepositoryComponent
    with LoggedUserProviderComponent
  =>

  val jenkinsService: JenkinsService = new JenkinsServiceImpl

  class JenkinsServiceImpl extends JenkinsService with FileApi with ParseFolder {
    private val jenkinsUrl = Play.configuration.getString("jenkins.url").get

    override def getUpdatedBuilds(existingBuilds: List[Build]): List[Build] = {
      val existingBuildsMap: Map[String, Build] = existingBuilds.map(x => (x.name, x)).toMap

      val allFolders = new Folder(directory).listFiles().filter(_.isDirectory).toList

      val newFolders: List[Folder] = allFolders.filterNot(x => existingBuildsMap.get(x.getName).isDefined)

      val foldersToUpdate: List[Folder] = existingBuilds.filter(_.status.isEmpty).map(x => new Folder(directory, x.name))

      val folders: List[Folder] = newFolders ++ foldersToUpdate

      val buildSources: List[BuildSource] = folders.flatMap(createBuildSource)

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
      val paramsFile = new File(folder, "Build/StartBuild/StartBuild.params")

      for {
        buildParams <- BuildParams(paramsFile)
        (buildNumber, prId) <- getBuildNumbers(folder.getName)
        branch <- prId.fold(branchRepository.getBranch(buildParams.branch.substring(7)))(f = id => branchRepository.getBranchByPullRequest(id))

      } yield BuildSource(branch.name, buildNumber, prId, folder, buildParams)
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
  }
}