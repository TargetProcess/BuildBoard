package models.jenkins

import components.{BranchRepositoryComponent, JenkinsServiceComponent}
import models.Build
import java.io.File

trait JenkinsServiceComponentImpl extends JenkinsServiceComponent {
  this: JenkinsServiceComponentImpl with BranchRepositoryComponent =>

  val jenkinsService = new JenkinsServiceImpl

  class JenkinsServiceImpl extends JenkinsService with FileApi with ParseFolder {


    override def getUpdatedBuilds(buildsToUpdate: Iterator[Build]): Iterator[Build] = {
      buildsToUpdate.flatMap(build => {
        val buildSource = BuildSource(build.branch, build.number, build.pullRequestId, new File(directory, build.name))
        val updatedBuild = getBuild(buildSource)
        updatedBuild
      })
    }

    override def getNewBuilds(existingBuilds: Iterator[Build]): Seq[Build] = {
      val existingNames: Set[String] = existingBuilds.map(_.name).toSet



      val allFolders = new File(directory).listFiles().filter(_.isDirectory).view
      val newFolders = allFolders.filterNot(x => existingNames(x.getName))

      newFolders.flatMap(createBuildSource).flatMap(getBuild)
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


    def createBuildSource(folder: File): Option[BuildSource] = {
      val paramsFile = new File(folder, "Build/StartBuild/StartBuild.params")

      for {
        buildParams <- BuildParams(paramsFile)
        (build, prId) <- getBuildNumbers(folder.getName)
        branch <- prId.map(id => branchRepository.getBranchByPullRequest(id)).getOrElse(branchRepository.getBranch(buildParams.branch.substring(7)))
      } yield BuildSource(branch.name, build, prId, folder)
    }

    // branch => origin/develop | origin/pr/1044/merge | origin/feature/us76314
    case class BuildParams(branch: String, parameters: Map[String, String])

    object BuildParams {
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
