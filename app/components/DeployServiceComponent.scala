package components

import models.Build
import models.buildActions.DeployBuildAction

import scala.concurrent.Future

trait DeployServiceComponent {
  val deployService : DeployService

  trait DeployService {
    def deployBuild(buildName: String, deployFolderName: String) : Future[Unit]
    def getDeployBuildActions(build:Build):List[DeployBuildAction]
  }
}
