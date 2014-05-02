package models.jenkins

import components.{BranchRepositoryComponent, JenkinsServiceComponent}
import models.{IBuildInfo, Build}
import java.io.File

trait JenkinsServiceComponentImpl extends JenkinsServiceComponent {
  this: JenkinsServiceComponentImpl with BranchRepositoryComponent =>

  val jenkinsService: JenkinsService = new JenkinsServiceImpl

  class JenkinsServiceImpl extends JenkinsService with FileApi with ParseFolder {


    override def getUpdatedBuilds(existingBuilds: List[IBuildInfo]): List[Build] = {
      val existingBuildsMap: Map[String, IBuildInfo] = existingBuilds.map(x => (x.name, x)).toMap


      val allFolders = new Folder(directory).listFiles().filter(_.isDirectory).toList

      val newFolders: List[Folder] = allFolders.filterNot(x => existingBuildsMap.get(x.getName).isDefined)

      val foldersToUpdate: List[Folder] = existingBuilds.filter(_.status.isEmpty).map(x => new Folder(directory, x.name))

      (newFolders ++ foldersToUpdate).flatMap(createBuildSource).flatMap(getBuild(_, existingBuildsMap))
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
        buildParams <- BuildParamsCompanion(paramsFile)
        (buildNumber, prId) <- getBuildNumbers(folder.getName)
        branch <- prId.fold(branchRepository.getBranch(buildParams.branch.substring(7)))(id => branchRepository.getBranchByPullRequest(id))
      } yield BuildSource(branch.name, buildNumber, prId, folder)
    }

    // branch => origin/develop | origin/pr/1044/merge | origin/feature/us76314
    case class BuildParams(branch: String, parameters: Map[String, String]) {

    }

    object BuildParamsCompanion {
      val branchNameR = "BRANCHNAME (.*)".r
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

  }


}
