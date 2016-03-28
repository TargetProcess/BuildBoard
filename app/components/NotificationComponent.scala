package components

import models.configuration.DeployConfig
import models.{Branch, Build}

import scala.util.Try

trait NotificationComponent {
  val notificationService: NotificationService

  trait NotificationService {
    def notifyAboutBuilds(builds: Iterator[Build])

    def notifyToggle(branch: Branch, build: Build)

    def notifyStartDeploy(channel: String, destination: String, build: Build)

    def notifyDoneDeploy(channel: String, destination: String, build: Build, result: Try[Any])
  }

}
