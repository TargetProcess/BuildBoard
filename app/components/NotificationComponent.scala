package components

import models.{IBuildInfo, Build, Branch}

trait NotificationComponent {
  val notificationService: NotificationService

  trait NotificationService {
    def notifyAboutBuilds(builds: List[Build])

    def notifyToggle(branch: Branch, build:IBuildInfo)
  }

}
