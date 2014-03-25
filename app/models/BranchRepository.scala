package models

import scala.language.postfixOps
import models.mongo.{Builds, Branches}
import com.mongodb.casbah.commons.MongoDBObject

class BranchRepository {
  private val buildRepo = new BuildRepository()

  def getBranch(id: String): Option[Branch] = Branches.findOne(MongoDBObject("name" -> id))

  def getBranches: List[BranchInfo] = {
    val builds = buildRepo.getBuildInfos

    Branches.find(MongoDBObject.empty)
      .toList
      .map(b => {
        val buildsForBranch = builds
          .filter(_.branch == b.name)
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

  def serialize(branch: BranchInfo) = {
    Some((branch.name, branch.url, branch.pullRequest, branch.entity, branch.lastBuild, branch.activity, branch.buildActions))
  }
}
