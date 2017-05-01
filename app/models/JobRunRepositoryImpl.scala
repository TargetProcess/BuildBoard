package models

import com.mongodb.casbah.Imports._
import com.novus.salat.dao.{ModelCompanion, SalatDAO}
import components.JobRunComponent
import org.joda.time.DateTime
import se.radley.plugin.salat._
import play.api.Play.current

trait JobRunRepositoryComponentImpl extends JobRunComponent {
  val jobRunRepository: JobRunRepository = new JobRunRepositoryImpl

  class JobRunRepositoryImpl extends JobRunRepository {

    import models.mongo.mongoContext._

    object JobRuns extends ModelCompanion[JobRun, Binders.ObjectId] {
      def collection = mongoCollection("jobRuns")

      val dao = new SalatDAO[JobRun, Binders.ObjectId](collection) {}

      collection.ensureIndex(DBObject("buildNumber" -> 1, "buildNode" -> 1), "build_number_node", unique = false)
      collection.ensureIndex(DBObject("startTime" -> 1), "startTime", unique = false)
    }

    override def getJobRuns(buildNumber: Int): Iterator[JobRun] =
      JobRuns.find(MongoDBObject("buildNumber" -> buildNumber))

    override def update(jobRun: JobRun): Unit = JobRuns
      .update(MongoDBObject("_id" -> jobRun.id), jobRun, upsert = true, multi = false, JobRuns.dao.collection.writeConcern)

    override def getJobRuns: Iterator[JobRun] = JobRuns.findAll()

    override def getJobRuns(buildNumber: Int, buildNode: String): Iterator[JobRun] =
      JobRuns.find(MongoDBObject("buildNumber" -> buildNumber, "buildNode" -> buildNode))

    override def removeOld(before: DateTime): Unit =
      JobRuns.remove("startTime" $lt before)
  }
}
