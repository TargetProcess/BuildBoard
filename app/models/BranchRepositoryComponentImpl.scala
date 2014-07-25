package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.{ModelCompanion, SalatDAO}
import components.{BranchRepositoryComponent, BuildRepositoryComponent}
import models.mongo.mongoContext
import models.mongo.mongoContext._
import play.api.Play.current
import se.radley.plugin.salat.Binders.ObjectId
import se.radley.plugin.salat._

import scala.language.postfixOps


trait BranchRepositoryComponentImpl extends BranchRepositoryComponent {

  this: BranchRepositoryComponentImpl with BuildRepositoryComponent =>

  val branchRepository: BranchRepository = new BranchRepositoryImpl

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
          .take(100)

        BranchInfo(b.name, b.url, b.pullRequest, b.entity, buildsForBranch.headOption, activity)
      })
    }

    def getBranch(id: String): Option[Branch] = Branches.findOne(MongoDBObject("name" -> id))

    def getBranches: List[Branch] = Branches.findAll().toList

    def remove(branch: Branch): Unit = Branches.remove(branch)

    def update(branch: Branch): Unit = Branches.update(MongoDBObject("name" -> branch.name), branch, upsert = true, multi = false, Branches.dao.collection.writeConcern)

    def getBranchByPullRequest(id: Int): Option[Branch] = Branches.findOne(MongoDBObject("pullRequest.prId" -> id))

    override def getBranchEntity(id: Int): Option[Branch] = Branches.findOne(MongoDBObject("entity._id" -> id))
  }

}