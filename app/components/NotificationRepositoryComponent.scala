package components

import org.joda.time.DateTime


trait NotificationRepositoryComponent {
  val notificationRepository: NotificationRepository

  trait NotificationRepository{
    def getLastNotification(branch: String): Option[Notification]
    def setLastNotification(branch: String, notification: Notification)
  }
}

case class Notification(branch:String, status:String, timestamp:DateTime)