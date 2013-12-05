package models.github

import models.tp.EntityRepo
import models._
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service._
import org.eclipse.egit.github.core.{RepositoryBranch, RepositoryId, PullRequest => GhPullRequest}
import scala.collection.JavaConverters._
import models.jenkins.JenkinsRepository
import scala.util.matching.Regex
import src.Utils._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

class GitHubRepository(implicit user: User) {
  val github = new GitHubClient().setOAuth2Token(user.githubToken)
  val repositoryService = new RepositoryService(github)
  val prService = new PullRequestService(github)

  val EntityBranchPattern = new Regex("^(?i)feature/(us|bug|f)(\\d+).*")
  val FeatureBranchPattern = new Regex("^(?i)feature/(\\w+)")
  val repo = new RepositoryId(GitHubApplication.user, GitHubApplication.repo)

  def getBranch(id: String): Branch = {

    val br: List[RepositoryBranch] = watch("get branches from github") {
      repositoryService.getBranches(repo).asScala.toList
    }

    val ghBranches: List[RepositoryBranch] = br.filter(_.getName == id)
    getBranchesInfo(ghBranches).head
  }


  def getBranches: List[Branch] = {
    if (user.githubToken == null) {
      Nil
    }
    else {
      val ghBranches: List[RepositoryBranch] = watch("Get branches from github") {
        repositoryService.getBranches(repo).asScala.toList
      }
      getBranchesInfo(ghBranches)
    }
  }

  def getBranchesInfo(ghBranches: List[RepositoryBranch]): List[Branch] = {


    val futurePullRequests: Future[Map[String, GhPullRequest]] = Future {
      val prList: List[GhPullRequest] =  watch("Get pull requests"){
        prService.getPullRequests(repo, "open").asScala.toList
      }
      prList.map(pr => (pr.getHead.getRef, pr)).toMap
    }


    val branchNames = ghBranches
      .map(br => br.getName)

    val entityIds = branchNames
      .flatMap {
      case EntityBranchPattern(_, id) => Some(id.toInt)
      case _ => None
    }
      .toList


    val futureEntities = Future {
      watch("Get assignables"){ new EntityRepo(user.token).getAssignables(entityIds)
        .map(e => (e.id, e))
        .toMap
      }
    }

    val aggFuture = for (a <- futurePullRequests;
                         b <- futureEntities) yield (a, b)


    val (ghPullRequests, entities) = Await.result(aggFuture, 10 seconds)

    ghBranches.map(githubBranch => {
      val name = githubBranch.getName

      val pullRequest = ghPullRequests.get(name)
        .map(pr => PullRequest.create(pr))


      val entity = name match {
        case EntityBranchPattern(_, id) => entities.get(id.toInt)
        case _ => None
      }

      val actions = List(
        BranchBuildAction(name, fullCycle = true),
        BranchBuildAction(name, fullCycle = false)
      ) ++ (pullRequest match {
        case Some(pr) => List(
          PullRequestBuildAction(pr.id, fullCycle = true),
          PullRequestBuildAction(pr.id, fullCycle = false)
        )
        case None => Nil
      })

      Branch(name, GitHubApplication.url(name), pullRequest, entity, actions)

    }).toList

  }

  def getPullRequestStatus(id: Int) = {
    val pr = prService.getPullRequest(repo, id)
    PullRequestStatus(pr.isMergeable, pr.isMerged)
  }
}
