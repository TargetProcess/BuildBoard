package models

import scala.language.postfixOps
import com.mongodb.casbah.commons.MongoDBObject
import components.{BuildRepositoryComponent, BranchRepositoryComponent}
import play.api.Play.current
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import se.radley.plugin.salat._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat.Binders.ObjectId
import models.mongo.mongoContext
import mongoContext._


trait BranchRepositoryComponentImpl extends BranchRepositoryComponent {

  this: BranchRepositoryComponentImpl with BuildRepositoryComponent =>

  val branchRepository = new BranchRepositoryImpl

  class BranchRepositoryImpl extends BranchRepository {

    object Branches extends ModelCompanion[Branch, ObjectId] {

      def collection = mongoCollection("branches")

      val dao = new SalatDAO[Branch, ObjectId](collection) {}

      // Indexes
      collection.ensureIndex(DBObject("name" -> 1), "", unique = true)
    }


    def getBranchInfos: List[BranchInfo] = {
      val builds = buildRepository.getBuildInfos.toList


      Branches.findAll()
        .toList
        .map(b => {
        val buildsForBranch = builds
          .filter(_.branch == b.name)
          .toList
          .sortBy(-_.number)
        val commits = buildsForBranch
          .flatMap(_.commits)
          .map(c => (c.timestamp, c))
          .groupBy(_._1)
          .map(_._2.head._2)

        val activity = (buildsForBranch ++ b.pullRequest ++ commits)
          .sortBy(-_.timestamp.getMillis)

        BranchInfo(b.name, b.url, b.pullRequest, b.entity, buildsForBranch.headOption, activity)
      })
    }

    def getBranch(id: String): Option[Branch] = Branches.findOne(MongoDBObject("name" -> id))

    def getBranches: List[Branch] = Branches.findAll().toList

    def remove(branch: Branch): Unit = Branches.remove(branch)

    def update(branch: Branch): Unit = Branches.update(MongoDBObject("name" -> branch.name), branch, upsert = true, multi = false, Branches.dao.collection.writeConcern)

    def getBranchByPullRequest(id: Int): Option[Branch] = Branches.findOne(MongoDBObject("pullRequest.prId" -> id))
  }

}