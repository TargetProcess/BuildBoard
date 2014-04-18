package models

import models.mongo.Builds
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import components.BuildRepositoryComponent


trait BuildRepositoryComponentImpl extends BuildRepositoryComponent {
  def buildRepository = new BuildRepositoryImpl

  class BuildRepositoryImpl extends BuildRepository {


    private val buildInfoProjection = MongoDBObject(
      "number" -> "number",
      "branch" -> "branch",
      "status" -> "status",
      "timestamp" -> "timestamp",
      "commits" -> "commits",
      "isPullRequest" -> "isPullRequest",
      "toggled" -> "toggled")

    def getBuilds: List[Build] = Builds.findAll().toList

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
  }

}