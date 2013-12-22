package models

import models.tp.EntityRepo
import scala.util.matching.Regex
import src.Utils._
import scala.language.postfixOps
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import models.github.{CachedGithubRepository, GithubBranch, RealGithubRepository, GithubRepository}

class BranchesRepository(implicit user: User) {

  val EntityBranchPattern = new Regex("^(?i)feature/(us|bug|f)(\\d+).*")
  val FeatureBranchPattern = new Regex("^(?i)feature/(\\w+)")

  val githubRepository: GithubRepository = new CachedGithubRepository


  def getBranch(id: String): Branch = {

    val br = watch("get branches from github") {
      githubRepository.getBranches
    }

    getBranchesInfo(br.filter(_.name == id)).head
  }


  def getBranches: List[Branch] = {
    if (user.githubToken == null) {
      Nil
    }
    else {
      val ghBranches = watch("Get branches from github") {
        githubRepository.getBranches
      }
      getBranchesInfo(ghBranches)
    }
  }

  def getBranchesInfo(ghBranches: List[GithubBranch]): List[Branch] = {


    val futurePullRequests = Future {
      val prList = watch("Get pull requests") {
        githubRepository.getPullRequests
      }
      prList.map(pr => (pr.name, pr)).toMap
    }


    val branchNames = ghBranches
      .map(br => br.name)

    val entityIds = branchNames
      .flatMap {
      case EntityBranchPattern(_, id) => Some(id.toInt)
      case _ => None
    }
      .toList


    val futureEntities = Future {
      watch("Get assignables") {
        new EntityRepo(user.token).getAssignables(entityIds)
          .map(e => (e.id, e))
          .toMap
      }
    }

    val aggFuture = for (a <- futurePullRequests;
                         b <- futureEntities) yield (a, b)


    val (ghPullRequests, entities) = Await.result(aggFuture, 10 seconds)

    ghBranches.map(githubBranch => {
      val name = githubBranch.name

      val pullRequest = ghPullRequests.get(name)

      val entity = name match {
        case EntityBranchPattern(_, id) => entities.get(id.toInt)
        case _ => None
      }

      val actions = List(
        BranchBuildAction(name, fullCycle = true),
        BranchBuildAction(name, fullCycle = false)
      ) ++ (pullRequest match {
        case Some(pr) => List(
          PullRequestBuildAction(pr.prId, fullCycle = true),
          PullRequestBuildAction(pr.prId, fullCycle = false)
        )
        case None => Nil
      })

      Branch(name, githubRepository.getUrlForBranch(name), pullRequest, entity, actions)

    }).toList

  }

  def getPullRequestStatus(id: Int): PullRequestStatus = githubRepository.getPullRequestStatus(id)
}
