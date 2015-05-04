package components

import java.io.File

import models.buildActions.{BuildAction, JenkinsBuildAction}
import models.{Branch, Build, BuildNode}

import scala.concurrent.Future
import scala.util.Try

trait JenkinsServiceComponent {

  val jenkinsService: JenkinsService


  trait JenkinsService {
    def getBuildActions(build: Build): List[BuildAction]

    def forceBuild(action: JenkinsBuildAction): Try[Any]

    def getUpdatedBuilds(existingBuilds: List[Build], buildNamesToUpdate: Seq[String]): List[Build]

    def getTestRun(branch: Branch, buildNumber: Int, part: String, run: String): Option[BuildNode]

    def getArtifact(file: String): File

    def deployBuild(buildName: String, deployFolderName: String) : Future[Unit]
  }

}
