package components

import java.io.File

import models.{Branch, Build, BuildNode}

import scala.util.Try

trait JenkinsServiceComponent {

  val jenkinsService: JenkinsService


  trait JenkinsService {
    def forceBuild(action: models.BuildAction): Try[String]

    def getUpdatedBuilds(existingBuilds:List[Build], buildNamesToUpdate: Seq[String]):List[Build]

    def getTestRun(branch: Branch, buildNumber: Int, part: String, run: String) : Option[BuildNode]

    def getArtifact(file: String): File
  }

}
