package models.deploy

import components._
import models.Build
import models.buildActions.DeployBuildAction
import models.jenkins.{FileApi, FileHelper}
import src.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DeployServiceComponentImpl extends DeployServiceComponent with FileHelper {
  this: DeployServiceComponentImpl
    with ConfigComponent
  =>

  override val deployService = new DeployService {

    lazy val deployDirectory = config.deployDirectoryRoot
    lazy val directory = config.jenkinsDataPath

    override def deployBuild(buildName: String, deployFolderName: String) = {


      val buildFolderPath: String = s"$directory\\$buildName\\Artifacts\\Code\\Releases\\"
      val deployFolderPath: String = s"$deployDirectory\\$deployFolderName\\"

      val buildFolder = new Folder(buildFolderPath)
      val deployFolder = new Folder(deployFolderPath)

      Future {
        Utils.watch(s"Deploy $buildFolderPath to $deployFolderPath") {
          for (file <- deployFolder.listFiles()) {
            file.delete()
          }

          for (file <- buildFolder.listFiles()) {
            if (file.getName.endsWith("archive.zip")) {
              FileApi.copyFile(file, deployFolder)
            }
          }
        }
      }
    }

    def canDeployBuild(buildName: String) = {
      val buildFolder = new Folder(s"$directory/$buildName/Artifacts/Code/Releases")
      buildFolder.exists
    }

    override def getDeployBuildActions(build: Build): List[DeployBuildAction] = if (canDeployBuild(build.name))
      config.teams.map(team => DeployBuildAction(build.name, build.number, team.name))
    else
      Nil

  }
}
