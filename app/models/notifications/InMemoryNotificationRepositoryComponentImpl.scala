package models.notifications

import components.{Notification, NotificationRepositoryComponent}

trait InMemoryNotificationRepositoryComponentImpl extends NotificationRepositoryComponent {
  override val notificationRepository: NotificationRepository = new NotificationRepository {
    val notificationMap = scala.collection.mutable.Map[String, Notification]()

    def getLastNotification(branch: String): Option[Notification] = {
      notificationMap.get(branch)
    }

    override def setLastNotification(branch: String, notification: Notification): Unit = {
      notificationMap(branch) = notification
    }
  }
}
