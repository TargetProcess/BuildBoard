package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.dao.{ModelCompanion, SalatDAO}
import components.BuildRepositoryComponent
import models.branches.Branch
import play.api.Play.current
import se.radley.plugin.salat.Binders.ObjectId
import se.radley.plugin.salat._

trait BuildRepositoryComponentImpl extends BuildRepositoryComponent {
  val buildRepository: BuildRepository = new BuildRepositoryImpl

  class BuildRepositoryImpl extends BuildRepository {

    import models.mongo.mongoContext._


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

    override def getBuilds(branch: Branch) = getBuilds(branch.name)

    override def getBuilds(branch: String) = Builds.find(MongoDBObject("branch" -> branch))

    override def getAllBuilds: Iterator[Build] = Builds.findAll()

    def getBuildInfos(branch: Branch) = findInner(MongoDBObject("branch" -> branch.name))

    private def findInner(predicate: DBObject) = Builds.find(predicate, buildInfoProjection)


    override def getBuild(branch: Branch, number: Int): Option[Build] = Builds.findOne(MongoDBObject("number" -> number, "branch" -> branch.name))

    override def toggleBuild(branch: Branch, number: Int, toggled: Boolean): Option[Build] = {
      val predicate = MongoDBObject("branch" -> branch.name, "number" -> number)
      val build = Builds.findOne(predicate)
        .map(b => b.copy(toggled = toggled))

      build.foreach(b => Builds.update(predicate, b, upsert = false, multi = false, Builds.dao.collection.writeConcern))

      build
    }


    override def removeAll(branch: Branch): Unit = Builds.find(MongoDBObject("branch" -> branch.name)).foreach(Builds.remove)

    override def update(build: Build): Unit = Builds.update(
      MongoDBObject(
        "number" -> build.number,
        "branch" -> build.branch),
      build, upsert = true, multi = false, Builds.dao.collection.writeConcern)

    override def getLastBuild(branch: Branch): Option[Build] = Builds.find(MongoDBObject("branch" -> branch.name))
      .sort(MongoDBObject("number" -> -1)).limit(1).toList.headOption

    override def getLastBuilds: Map[String, Build] = {


      val mapF: JSFunction =
        """
          |function(){
          |  	var r = this;
          |	  emit(r.branch, r);
          |}
        """.stripMargin


      val reduceF: JSFunction =
        """
          |function Reduce(key, values) {
          |
          |	var x = values.shift();
          |
          |	values.forEach(function(v) {
          |		if (x.number < v.number) {
          |			x = v;
          |		}
          |	});
          |
          |
          |	return x;
          |}
        """.stripMargin

      Builds.collection.mapReduce(mapF, reduceF, MapReduceInlineOutput)
        .map(res => grater[ResultMapReduce].asObject(res))
        .map(res => (res._id, res.value))
        .toMap

    }
  }


}

case class ResultMapReduce(_id: String, value: Build)
