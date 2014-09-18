package components

import models.Build
import models.branches.Branch

trait NotificationComponent {
  val notificationService: NotificationService

  trait NotificationService {
    def notifyAboutBuilds(builds: List[Build])
    def notifyToggle(branch: Branch, build:Build)
  }

}
