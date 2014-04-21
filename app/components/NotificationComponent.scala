package components

import models.{IBuildInfo, Build, Branch, BuildInfo}

trait NotificationComponent {
  val notificationService: NotificationService

  trait NotificationService {
    def notifyAboutBuilds(branch: Branch, builds: List[Build])

    def notifyToggle(branch: Branch, build:IBuildInfo)
  }

}
