package models.github

import models._
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service._
import org.eclipse.egit.github.core.RepositoryId
import scala.collection.JavaConverters._
import org.joda.time.DateTime
import models.mongo.{GithubBranches, PullRequests}


class RealGithubRepository(implicit user: User) extends GithubRepository{
  val github = new GitHubClient().setOAuth2Token(user.githubToken)
  val repositoryService = new RepositoryService(github)
  val prService = new PullRequestService(github)
  val commitService = new CommitService(github)
  val repo = new RepositoryId(GithubApplication.user, GithubApplication.repo)

  def getBranches: List[Branch] = repositoryService.getBranches(repo).asScala.toList.map(GithubBranch.create _)

  def getPullRequests: List[PullRequest] = prService.getPullRequests(repo, "open").asScala.toList.map(GithubPullRequest.create _)

  def getPullRequestStatus(id: Int) = {
    val pr = prService.getPullRequest(repo, id)
    PullRequestStatus(pr.isMergeable, pr.isMerged)
  }

  override def getCommits(hashes: List[String]): List[Commit] = hashes.map(commitService.getCommit(repo, _)).map(c => Commit(c.getSha, c.getCommit.getMessage, c.getCommitter.getEmail, new DateTime(c.getCommitter.getCreatedAt)))
}


class CachedGithubRepository(implicit user: User) extends RealGithubRepository{
  override def getPullRequests: List[PullRequest] = PullRequests.findAll().toList
  override def getBranches: List[Branch] = GithubBranches.findAll().toList
}
