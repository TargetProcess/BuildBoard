package models.buildWatcher

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{ModelCompanion, SalatDAO}
import components.RerunRepositoryComponent
import models.Build
import play.api.Play.current
import se.radley.plugin.salat.Binders.ObjectId
import se.radley.plugin.salat._

trait RerunRepositoryComponentImpl extends RerunRepositoryComponent {
  final override val rerunRepository = new RerunRepository {


    import models.mongo.mongoContext._


    private object Reruns extends ModelCompanion[RerunInfo, ObjectId] {
      def collection = mongoCollection("notifications")
      val dao = new SalatDAO[RerunInfo, ObjectId](collection) {}
    }


    override def contains(build: Build, category: String, part: String): Boolean = Reruns.findOne(
      MongoDBObject(
        "build" -> build.name,
        "category" -> category,
        "part" -> part
      )
    ).isDefined

    override def markAsRerun(build: Build, category: String, parts: List[String]) = {
      parts.foreach(part => {
        Reruns.update(MongoDBObject(
          "build" -> build.name,
          "category" -> category,
          "part" -> part
        ), RerunInfo(build.name, category, part), upsert = true, multi = false, Reruns.dao.collection.writeConcern)
      })
    }
  }
}

case class RerunInfo(build: String, category: String, part: String)
