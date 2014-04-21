package components

import models.{Build, Branch, BuildInfo}

trait NotificationComponent {
  val notificationService: NotificationService

  trait NotificationService {
    def notifyBuilds(branch: Branch, builds: List[Build])

    def notifyToggle(branch: Branch, build:BuildInfo)
  }

}
