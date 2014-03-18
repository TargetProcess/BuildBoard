package models

import models.tp.EntityRepo
import scala.util.matching.Regex
import src.Utils._
import scala.language.postfixOps
import models.github._
import scala.Some

class BranchesRepository(implicit user: User) {

  val EntityBranchPattern = new Regex("^(?i)feature/(us|bug|f)(\\d+).*")
  val FeatureBranchPattern = new Regex("^(?i)feature/(\\w+)")

  val githubRepository: GithubRepository = new CachedGithubRepository


  def getBranch(id: String): Option[Branch] = {

    val br = watch("get branches from github") {
      githubRepository.getBranches
    }

    br.filter(_.name == id).headOption
//    getBranchesInfo(br.filter(_.name == id)).headOption
  }

  def getBranches: List[Branch] = {
    if (user.githubToken == null) {
      Nil
    }
    else {
      val ghBranches = watch("Get branches from github") {
        githubRepository.getBranches
      }

      ghBranches
//      getBranchesInfo(ghBranches)
    }
  }

  def getBranchesInfo(ghBranches: List[Branch]): List[Branch] = {
    val branchNames = ghBranches
      .map(br => br.name)

    val entityIds = branchNames
      .flatMap {
      case EntityBranchPattern(_, id) => Some(id.toInt)
      case _ => None
    }
      .toList

    val entities =
        new EntityRepo(user.token).getAssignables(entityIds)
          .map(e => (e.id, e))
          .toMap

    ghBranches.map(githubBranch => {
      val name = githubBranch.name

      val pullRequest = githubBranch.pullRequest

      val entity = name match {
        case EntityBranchPattern(_, id) => entities.get(id.toInt)
        case _ => None
      }

      val actions = List(
        BranchBuildAction(name, BuildPackageOnly),
        BranchBuildAction(name, FullCycle),
        BranchBuildAction(name, ShortCycle)
      ) ++ (pullRequest match {
        case Some(pr) => List(
          PullRequestBuildAction(pr.prId, FullCycle),
          PullRequestBuildAction(pr.prId, ShortCycle)
        )
        case None => Nil
      })

      //todo: add actions to branch
      Branch(name, githubBranch.url, pullRequest, entity/*, actions*/)

    }).toList

  }

  //todo
  def getPullRequestStatus(id: Int): PullRequestStatus = ??? // githubRepository.getPullRequestStatus(id)
}
