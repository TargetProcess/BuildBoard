package models.notifications

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{ModelCompanion, SalatDAO}
import components.{Notification, NotificationRepositoryComponent}
import se.radley.plugin.salat.Binders.ObjectId
import se.radley.plugin.salat._
import play.api.Play.current

trait NotificationRepositoryComponentImpl extends NotificationRepositoryComponent {
  override val notificationRepository: NotificationRepository = new NotificationRepository {

    import models.mongo.mongoContext._


    object Notifications extends ModelCompanion[Notification, ObjectId] {
      def collection = mongoCollection("notifications")

      val dao = new SalatDAO[Notification, ObjectId](collection) {}

      collection.ensureIndex(DBObject("branch" -> 1), "branch_name", unique = true)
    }

    override def getLastNotification(branch: String): Option[Notification] = Notifications.findOne(MongoDBObject("branch" -> branch))

    override def setLastNotification(branch: String, notification: Notification): Unit = Notifications.update(
      MongoDBObject("branch" -> branch),
      notification, upsert = true, multi = false, Notifications.dao.collection.writeConcern)
  }
}
