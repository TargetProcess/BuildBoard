package components

import models.{IBuildInfo, Branch}

trait NotificationComponent {
  val notificationService: NotificationService

  trait NotificationService {
    def notifyAboutBuilds(builds: List[IBuildInfo])
    def notifyToggle(branch: Branch, build:IBuildInfo)
  }

}
