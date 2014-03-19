package models

import scala.language.postfixOps
import models.mongo.Branches
import com.mongodb.casbah.commons.MongoDBObject

class BranchesRepository(implicit user: User) {

  def getBranch(id: String): Option[Branch] = if (user.githubToken == null) None
  else Branches.findOne(MongoDBObject("name" -> id))

  def getBranches: List[Branch] = if (user.githubToken == null) Nil
  else Branches.findAll().toList

  //  def getBranchesInfo(ghBranches: List[Branch]): List[Branch] = {
  //    val branchNames = ghBranches
  //      .map(br => br.name)
  //
  //    val entityIds = branchNames
  //      .flatMap {
  //      case EntityBranchPattern(_, id) => Some(id.toInt)
  //      case _ => None
  //    }
  //      .toList
  //
  //    val entities =
  //        new EntityRepo(user.token).getAssignables(entityIds)
  //          .map(e => (e.id, e))
  //          .toMap
  //
  //    ghBranches.map(githubBranch => {
  //      val name = githubBranch.name
  //
  //      val pullRequest = githubBranch.pullRequest
  //
  //      val entity = name match {
  //        case EntityBranchPattern(_, id) => entities.get(id.toInt)
  //        case _ => None
  //      }
  //
  //      val actions = List(
  //        BranchBuildAction(name, BuildPackageOnly),
  //        BranchBuildAction(name, FullCycle),
  //        BranchBuildAction(name, ShortCycle)
  //      ) ++ (pullRequest match {
  //        case Some(pr) => List(
  //          PullRequestBuildAction(pr.prId, FullCycle),
  //          PullRequestBuildAction(pr.prId, ShortCycle)
  //        )
  //        case None => Nil
  //      })
  //
  //      //todo: add actions to branch
  //      Branch(name, githubBranch.url, pullRequest, entity/*, actions*/)
  //
  //    }).toList
  //
  //  }
}
