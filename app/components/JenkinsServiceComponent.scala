package components

import scala.util.Try
import models._
import java.io.File
import models.BuildNode
import models.Branch
import models.Build

trait JenkinsServiceComponent {

  val jenkinsService: JenkinsService


  trait JenkinsService {
    def forceBuild(action: models.BuildAction): Try[String]

    def getUpdatedBuilds(existingBuilds:List[IBuildInfo]):List[Build]

    def getTestRun(branch: Branch, buildNumber: Int, part: String, run: String) : Option[BuildNode]

    def getArtifact(file: String): File
  }

}
