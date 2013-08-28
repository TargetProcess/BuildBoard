package models.GitHub

import org.kohsuke.github.{GHPullRequest, GHIssueState, GHBranch, GitHub}
import collection.JavaConversions._
import models.User
import models.tp.EntityRepo
import models.{PullRequest, Branch, User}

class GitHubRepository(implicit user: User) {
  private val github = GitHub.connectUsingOAuth(user.githubToken)
  private val repo = github.getRepository("TargetProcess/TP")

  def getBranches: List[Branch] = {
    val ghBranches = repo.getBranches.values.toList
    val pullRequests = repo.getPullRequests(GHIssueState.OPEN)
      .map(pr => (pr.getHead.getRef, pr))
      .toMap

    val branches = ghBranches.map(x => {
      val name = x.getName
      val pullRequest = pullRequests
        .get(name)
        .map(pr => PullRequest(pr.getNumber, pr.getUrl.toString))
      Branch.create(name, pullRequest)
    })

    /*val entityIds = branches.flatMap {
      case EntityBranch(_, entity) => Some(entity.id)
      case _ => None
    }

    val entities = EntityRepo.getAssignables(entityIds).map(e => (e.id, e))
    */

    branches
  }
}
