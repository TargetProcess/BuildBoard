package components

import scala.util.Try
import models.Build

trait JenkinsServiceComponent {

  val jenkinsService: JenkinsService


  trait JenkinsService {
    def forceBuild(action: models.BuildAction): Try[String]

    def getUpdatedBuilds(existingBuilds:Iterator[Build]):Iterator[Build]

    def getNewBuilds(existingBuilds:Iterator[Build]):Iterator[Build]


    //def getBuildNames:List[String]

    //def getBuilds(branch: models.Branch): List[Build]
  }

}
