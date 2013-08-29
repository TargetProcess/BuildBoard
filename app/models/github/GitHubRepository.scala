package models.github

import collection.JavaConversions._
import models.tp.EntityRepo
import models._
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service._
import org.eclipse.egit.github.core.RepositoryId

class GitHubRepository(implicit user: User) {
  val github = new GitHubClient().setOAuth2Token(user.githubToken)
  val repositoryService = new RepositoryService(github)
  val prService = new PullRequestService(github)

  val EntityBranchPattern = "^(?i)feature/(us|bug|f)(\\d+).*".r
  val FeatureBranchPattern = "^(?i)feature/(\\w+)".r
  val repo = new RepositoryId("TargetProcess", "TP")


  def getBranches: List[Branch] = {
    val ghBranches = repositoryService.getBranches(repo)
    val ghPullRequests = prService.getPullRequests(repo, "OPEN").map(pr => (pr.getHead.getRef, pr)).toMap

    val branchNames = ghBranches
      .map(br => br.getName)

    val entityIds = branchNames
      .flatMap {
      case EntityBranchPattern(_, id) => Some(id.toInt)
      case _ => None
    }
      .toList

    val entities = new EntityRepo(user).getAssignables(entityIds)
      .map(e => (e.id, e))
      .toMap

    ghBranches.map(githubBranch => {
      val name = githubBranch.getName

      val pullRequest = ghPullRequests.get(name)
        .map(pr => PullRequest(pr))


      val entity = name match {
        case EntityBranchPattern(_, id) => entities.get(id.toInt)
        case _ => None
      }

      Branch(name, pullRequest, entity)

    }).toList
  }

  def getPullRequestStatus(id: Int) = {
    var pr = prService.getPullRequest(repo, id)
    PullRequestStatus(pr.isMergeable, pr.isMerged)
  }

  def getBranch(name: String) = {
    val ghBranches = repositoryService.getBranches(repo).find(p => p.getName == name)
    ghBranches
  }



}
