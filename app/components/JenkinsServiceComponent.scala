package components

import java.io.File

import models.buildActions.JenkinsBuildAction
import models.{Branch, Build, BuildNode}

import scala.util.Try

trait JenkinsServiceComponent {

  val jenkinsService: JenkinsService


  trait JenkinsService {
    def getBuildActions(build: Build): List[JenkinsBuildAction]

    def getUpdatedBuilds(existingBuilds: List[Build], buildNamesToUpdate: Seq[String]): Stream[Build]

    def getTestRun(branch: Branch, buildNumber: Int, part: String, run: String): Option[BuildNode]

    def getArtifact(file: String): File

  }

}



