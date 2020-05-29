package buildboard2.components

import play.api.Play.current
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders.ObjectId
import buildboard2.model.Job2
import models.mongo.mongoContext

trait Job2RepositoryComponentImpl extends Job2RepositoryComponent{
  val job2Repository = new Job2RepositoryImpl

  class Job2RepositoryImpl extends Job2Repository {

    import mongoContext.context

    object Jobs2 extends ModelCompanion[Job2, ObjectId] {
      def collection = mongoCollection("jobs2")

      val dao = new SalatDAO[Job2, ObjectId](collection) {}
    }

    override def save(job: Job2): Unit = {
      Jobs2.remove(MongoDBObject("id" -> job.id))
      Jobs2.save(job)
    }

    override def getAll: Iterator[Job2] = Jobs2.findAll()

    override def count: Long = Jobs2.count()
  }
}
