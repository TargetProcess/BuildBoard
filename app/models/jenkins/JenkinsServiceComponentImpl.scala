package models.jenkins

import components.{BuildRepositoryComponent, BranchRepositoryComponent, JenkinsServiceComponent}
import models.{Branch, IBuildInfo, Build}
import java.io.File

trait JenkinsServiceComponentImpl extends JenkinsServiceComponent {
  this: JenkinsServiceComponentImpl with BranchRepositoryComponent with BuildRepositoryComponent =>

  val jenkinsService: JenkinsService = new JenkinsServiceImpl

  class JenkinsServiceImpl extends JenkinsService with FileApi with ParseFolder {


    override def getUpdatedBuilds(existingBuilds: List[IBuildInfo]): List[Build] = {
      val existingBuildsMap: Map[String, IBuildInfo] = existingBuilds.map(x => (x.name, x)).toMap

      val allFolders = new Folder(directory).listFiles().filter(_.isDirectory).toList

      val newFolders: List[Folder] = allFolders.filterNot(x => existingBuildsMap.get(x.getName).isDefined)

      val foldersToUpdate: List[Folder] = existingBuilds.filter(_.status.isEmpty).map(x => new Folder(directory, x.name))

      val folders: List[Folder] = newFolders ++ foldersToUpdate

      val buildSources: List[BuildSource] = folders.flatMap(createBuildSource)

      val result: List[Build] = buildSources.flatMap(getBuild(_, existingBuildsMap))

      result

    }

    def getBuildNumbers(name: String): Option[(Int, Option[Int])] = {
      val prR = """pr_(\d+)_(\d+)""".r
      val buildR = """.*_(\d+)$""".r
      play.Logger.info(name)
      val result = name match {
        case prR(prID, build) => Some((build.toInt, Some(prID.toInt)))
        case buildR(build) => Some((build.toInt, None))
        case _ => None
      }

      play.Logger.info(result.toString)
      result
    }


    def createBuildSource(folder: Folder): Option[BuildSource] = {
      val paramsFile = new File(folder, "Build/StartBuild/StartBuild.params")

      for {
        buildParams <- BuildParamsCompanion(paramsFile)

        (buildNumber, prId) <- getBuildNumbers(folder.getName)
        branch <- prId.fold(branchRepository.getBranch(buildParams.branch.substring(7)))(f = id => branchRepository.getBranchByPullRequest(id))

      } yield BuildSource(branch.name, buildNumber, prId, folder, buildParams)
    }


    def findBuild(branch: Branch, buildId: Int):Option[Build] = buildRepository.getBuild(branch, buildId)

    def getTestRun(branch: Branch, build: Int, part: String, run: String) = findBuild(branch, build)
      .map(b => b.getTestRunBuildNode(part, run))
      .flatten
      .map(testRunBuildNode => testRunBuildNode.copy(testResults = getTestCasePackages(testRunBuildNode)))


  }


}

// branch => origin/develop | origin/pr/1044/merge | origin/feature/us76314
case class BuildParams(branch: String, parameters: Map[String, String]) {

}

object BuildParamsCompanion extends FileApi {
  val branchNameR = "BRANCHNAME: (.*)".r
  val paramR = "([^:]*): (.*)".r

  def apply(file: File) = read(file).flatMap(str => {
    val lines = str.split('\n')

    val name = lines(0) match {
      case branchNameR(n) => Some(n)
      case _ => None
    }


    val parameters = lines.drop(1)
      .flatMap {
      case paramR(key, value) => Some((key, value))
    }.toMap

    name.map(n => BuildParams(n, parameters))
  })
}

