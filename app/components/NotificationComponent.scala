package components

import models.{Build, Branch}

trait NotificationComponent {
  val notificationService: NotificationService

  trait NotificationService {
    def notifyAboutBuilds(builds: List[Build])
    def notifyToggle(branch: Branch, build:Build)
  }

}
