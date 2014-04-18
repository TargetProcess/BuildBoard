package models

import models.mongo.mongoContext
import com.mongodb.casbah.commons.MongoDBObject
import components.BuildRepositoryComponent
import play.api.Play.current
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import se.radley.plugin.salat._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat.Binders.ObjectId


trait BuildRepositoryComponentImpl extends BuildRepositoryComponent {
  val buildRepository: BuildRepository = new BuildRepositoryImpl

  class BuildRepositoryImpl extends BuildRepository {

    import mongoContext._


    object Builds extends ModelCompanion[Build, ObjectId] {
      def collection = mongoCollection("builds")

      val dao = new SalatDAO[Build, ObjectId](collection) {}

      // Indexes
      collection.ensureIndex(DBObject("number" -> 1, "branch" -> 1), "build_number", unique = true)
    }


    private val buildInfoProjection = MongoDBObject(
      "number" -> "number",
      "branch" -> "branch",
      "status" -> "status",
      "timestamp" -> "timestamp",
      "commits" -> "commits",
      "isPullRequest" -> "isPullRequest",
      "toggled" -> "toggled")

    def getBuilds(branch: Branch): List[Build] = Builds.find(MongoDBObject("branch" -> branch.name)).toList

    def getBuildInfos: List[BuildInfo] = findInner(MongoDBObject.empty)

    def getBuildInfos(branch: Branch): List[BuildInfo] = findInner(MongoDBObject("branch" -> branch.name))

    private def findInner(predicate: DBObject) = Builds.find(predicate, buildInfoProjection)
      .map(toBuildInfo)
      .toList

    def getBuild(branch: Branch, number: Int): Option[Build] = Builds.findOne(MongoDBObject("number" -> number, "branch" -> branch.name))

    def toggleBuild(branch: Branch, number: Int, toggled: Boolean): Option[BuildInfo] = {
      val predicate = MongoDBObject("branch" -> branch.name, "number" -> number)
      val build = Builds.findOne(predicate)
        .map(b => b.copy(toggled = toggled))

      build.foreach(b => Builds.update(predicate, b, upsert = false, multi = false, Builds.dao.collection.writeConcern))

      build.map(toBuildInfo)
    }

    private def toBuildInfo(b: Build): BuildInfo = BuildInfo(b.number, b.branch, b.status, b.timestamp, b.isPullRequest, b.toggled, b.commits)

    override def removeAll(branch: Branch): Unit = Builds.find(MongoDBObject("branch" -> branch.name)).foreach(Builds.remove)

    override def update(branch: Branch, build: Build): Unit = Builds.update(
      MongoDBObject(
        "number" -> build.number,
        "branch" -> branch.name),
      build, upsert = true, multi = false, Builds.dao.collection.writeConcern)

  }

}