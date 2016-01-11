package buildboard2.components

import buildboard2.model.Artifact2
import com.mongodb.casbah.commons.MongoDBObject
import play.api.Play.current
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import models.mongo.mongoContext
import se.radley.plugin.salat.Binders._
import se.radley.plugin.salat._

trait Artifact2RepositoryComponentImpl extends Artifact2RepositoryComponent {
  val artifact2Repository = new Artifact2RepositoryImpl

  class Artifact2RepositoryImpl extends Artifact2Repository {

    import mongoContext.context

    object Artifacts2 extends ModelCompanion[Artifact2, ObjectId] {
      def collection = mongoCollection("artifacts2")

      val dao = new SalatDAO[Artifact2, ObjectId](collection) {}
    }

    override def save(artifact: Artifact2): Unit = {
      Artifacts2.remove(MongoDBObject("id" -> artifact.id))
      Artifacts2.save(artifact)
    }

    override def getAll: Iterator[Artifact2] = Artifacts2.findAll()

    override def count: Long = Artifacts2.count()
  }

}
