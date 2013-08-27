package models

import org.kohsuke.github.{GHPullRequest, GHIssueState, GHBranch, GitHub}
import collection.JavaConversions._

class GitHubRepository(accessToken: String) {
  private val github = GitHub.connectUsingOAuth(accessToken)
  private val repo = github.getRepository("TargetProcess/TP")
  def getBranches: Iterable[Branch] = {
    val branches: Iterable[GHBranch] = repo.getBranches.values
    val pullRequests = repo.getPullRequests(GHIssueState.OPEN)

    branches.map(x => {
      val name = x.getName
      val pullRequest: Option[PullRequest] = pullRequests.filter(p => p.getHead.getRef == name).headOption match {
        case Some(pr: GHPullRequest) => {
          Some(new PullRequest(pr.getNumber, pr.getUrl.toString))
        }
        case None => None
      }
      Branch.create(name, pullRequest)
    })
  }
}
