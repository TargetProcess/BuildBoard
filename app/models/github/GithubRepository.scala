package models.github

import models._
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service._
import scala.collection.JavaConverters._
import org.joda.time.DateTime
import org.eclipse.egit.github.core.{PullRequest => PR, RepositoryId}
import models.{PullRequestStatus, PullRequest}

class GithubRepository(implicit user: AuthInfo) {
  private val github = new GitHubClient().setOAuth2Token(user.githubToken)
  private val repositoryService = new RepositoryService(github)
  private val prService = new PullRequestService(github)
  private val commitService = new CommitService(github)
  private val repo = new RepositoryId(GithubApplication.user, GithubApplication.repo)

  def getBranches: List[Branch] = repositoryService.getBranches(repo).asScala.toList.map(createBranch)

  def getPullRequests: List[PullRequest] = prService.getPullRequests(repo, "open").asScala.toList.map(createPullRequest)

  def getPullRequestStatus(id: Int) = {
    val pr = prService.getPullRequest(repo, id)
    PullRequestStatus(pr.isMergeable, pr.isMerged)
  }

  def getCommits(hashes: List[String]): List[Commit] = hashes
    .map(commitService.getCommit(repo, _))
    .map(c => Commit(c.getSha, c.getCommit.getMessage, c.getCommitter.getEmail, new DateTime(c.getCommitter.getCreatedAt)))

  private def createBranch(branch:org.eclipse.egit.github.core.RepositoryBranch) = Branch(branch.getName, GithubApplication.url(branch.getName))

  private def createPullRequest(pr: PR): PullRequest = PullRequest(pr.getHead.getRef, pr.getNumber, pr.getHtmlUrl, new DateTime(pr.getCreatedAt), PullRequestStatus(pr.isMergeable, pr.isMerged))
}
