package components

import models.BuildInfo

trait NotificationComponent {
  val notificationService: NotificationService

  trait NotificationService {
    def notifyToggle(build:BuildInfo)
  }

}
