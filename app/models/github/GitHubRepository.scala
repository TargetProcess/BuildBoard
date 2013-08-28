package models.github

import org.kohsuke.github.{ GHIssueState, GitHub }
import collection.JavaConversions._
import models.{ PullRequest, Branch, User }
import models.tp.EntityRepo
import models._

class GitHubRepository(implicit user: User) {
  val EntityBranchPattern = "^(?i)feature/(us|bug|f)(\\d+).*".r
  val FeatureBranchPattern = "^(?i)feature/(\\w+)".r

  private val github = GitHub.connectUsingOAuth(user.githubToken)
  private val repo = github.getRepository("TargetProcess/TP")

  def getBranches: List[Branch] = {
    val ghBranches = repo.getBranches.values.toList
    val ghPullRequests = repo.getPullRequests(GHIssueState.OPEN)
      .map(pr => (pr.getHead.getRef, pr))
      .toMap

    val entityIds = ghBranches
      .map(br => br.getName)
      .flatMap {
        case EntityBranchPattern(_, id) => Some(id.toInt)
        case _ => None
      }

    val entities = EntityRepo.getAssignables(entityIds).get
      .map(e => (e.id, e))
      .toMap

    ghBranches.map(githubBranch => {
      val name = githubBranch.getName
      val pullRequest = ghPullRequests.get(name)
        .map(pr => PullRequest(pr.getNumber, pr.getUrl.toString))

      val branch = name match {
        case EntityBranchPattern(_, id) => EntityBranch(name, entities.get(id.toInt))
        case FeatureBranchPattern(feature) => FeatureBranch(name, feature)
        case _ => RegularBranch(name)
      }
      branch.pullRequest = pullRequest
      branch
    })
  }
}
