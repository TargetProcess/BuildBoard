package buildboard2.components

import play.api.Play.current
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders.ObjectId
import buildboard2.model.Artifact2
import models.mongo.mongoContext
import models.jenkins.Artifacts
import components.ConfigComponent

trait Artifact2RepositoryComponentImpl extends Artifact2RepositoryComponent {
  this: ConfigComponent =>
  val artifact2Repository = new Artifact2RepositoryImpl

  class Artifact2RepositoryImpl extends Artifact2Repository with Artifacts {

    import mongoContext.context

    lazy val directory = config.jenkinsDataPath
    lazy val deployDirectory = config.deployDirectoryRoot

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
