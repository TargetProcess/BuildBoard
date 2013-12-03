package models.github

import models.tp.EntityRepo
import models._
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service._
import org.eclipse.egit.github.core.{RepositoryBranch, RepositoryId, PullRequest=>GhPullRequest}
import scala.collection.JavaConverters._
import models.jenkins.JenkinsRepository
import scala.util.matching.Regex

class GitHubRepository(implicit user: User) {
  val github = new GitHubClient().setOAuth2Token(user.githubToken)
  val repositoryService = new RepositoryService(github)
  val prService = new PullRequestService(github)

  val EntityBranchPattern = new Regex("^(?i)feature/(us|bug|f)(\\d+).*")
  val FeatureBranchPattern = new Regex("^(?i)feature/(\\w+)")
  val repo = new RepositoryId("TargetProcess", "TP")

  def getBranch(id:String):Branch = {
    val br: List[RepositoryBranch] = repositoryService.getBranches(repo).asScala.toList
    val ghBranches:List[RepositoryBranch] = br.filter(_.getName == id)
    getBranchesInfo(ghBranches).head
  }



  def getBranches: List[Branch] = {
    if (user.githubToken == null) {
      Nil
    }
    else {
      val ghBranches:List[RepositoryBranch] = repositoryService.getBranches(repo).asScala.toList
      getBranchesInfo(ghBranches)
    }
  }

  def getBranchesInfo(ghBranches: List[RepositoryBranch]): List[Branch] ={

    val prList:List[GhPullRequest] = prService.getPullRequests(repo, "OPEN").asScala.toList

    val ghPullRequests:Map[String,GhPullRequest] = prList.map(pr => (pr.getHead.getRef, pr)).toMap


    val branchNames = ghBranches
      .map(br => br.getName)

    val entityIds = branchNames
      .flatMap {
      case EntityBranchPattern(_, id) => Some(id.toInt)
      case _ => None
    }
      .toList

    val entities = new EntityRepo(user.token).getAssignables(entityIds)
      .map(e => (e.id, e))
      .toMap

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
      }     )

      Branch(name, pullRequest, entity, actions)

    }).toList

  }

  def getPullRequestStatus(id: Int) = {
    val pr = prService.getPullRequest(repo, id)
    PullRequestStatus(pr.isMergeable, pr.isMerged)
  }
}
