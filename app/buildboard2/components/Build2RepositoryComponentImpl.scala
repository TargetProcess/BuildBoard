package buildboard2.components

import buildboard2.model.Build2
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import models.mongo.mongoContext
import se.radley.plugin.salat.Binders._
import se.radley.plugin.salat._
import play.api.Play.current

trait Build2RepositoryComponentImpl extends Build2RepositoryComponent {
  val build2Repository = new Build2RepositoryImpl

  class Build2RepositoryImpl extends Build2Repository {

    import mongoContext.context

    object Builds2 extends ModelCompanion[Build2, ObjectId] {
      def collection = mongoCollection("builds2")

      val dao = new SalatDAO[Build2, ObjectId](collection) {}
    }

    override def save(build: Build2): Unit = {
      Builds2.remove(MongoDBObject("id" -> build.id))
      Builds2.save(build)
    }

    override def getAll: Iterator[Build2] = Builds2.findAll()

    override def count: Long = Builds2.count()
  }

}
