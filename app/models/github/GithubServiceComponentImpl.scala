package models.github

import components._
import models.{PullRequest, PullRequestStatus, _}
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service._
import org.eclipse.egit.github.core.{CommitStatus, PullRequest => PR, RepositoryId}
import org.joda.time.DateTime

import scala.collection.JavaConverters._

trait GithubServiceComponentImpl extends GithubServiceComponent {
  this: GithubServiceComponentImpl
    with AuthInfoProviderComponent
  =>

  override val githubService: GithubService = new GithubServiceImpl(authInfo)

  class GithubServiceImpl(authInfo: AuthInfo) extends GithubService {
    private val github = new GitHubClient().setOAuth2Token(authInfo.githubToken)
    private val repositoryService = new RepositoryService(github)
    private val prService = new PullRequestService(github)
    private val repo = new RepositoryId(GithubApplication.user, GithubApplication.repo)
    private val referenceService = new ReferenceService(github)
    private val commitService = new CommitService(github)
    private val issueService = new IssueService(github)

    def getBranches: List[Branch] = repositoryService.getBranches(repo).asScala.map(b => createBranch(b)).toList

    def getPullRequests: List[PullRequest] = prService.getPullRequests(repo, "open").asScala.map(parsePullRequest).toList

    def minOptionBy[A, B: Ordering](seq: Seq[A])(f: A => B) =
      seq reduceOption Ordering.by(f).min

    def getPullRequestStatus(id: Int) = {
      val pr = prService.getPullRequest(repo, id)

      val isLgtm = isReviewed(id)

      PullRequestStatus(pr.isMergeable, pr.isMerged, isLgtm = isLgtm)
    }

    def isReviewed(id: Int): Boolean = {
      val commitsM = util.Try(prService.getCommits(repo, id).asScala).toOption
      val commentsM = util.Try(issueService.getComments(repo, id).asScala).toOption


      val isLgtm = for (commits <- commitsM;
                        comments <- commentsM;
                        lastCommitDate = commits.map(_.getCommit.getCommitter.getDate).max;
                        lastComment <- minOptionBy(comments.filter(_.getBody.toLowerCase.contains("lgtm")))(_.getCreatedAt)
                        if new DateTime(lastComment.getCreatedAt).isAfter(new DateTime(lastCommitDate))
      )
        yield true

      isLgtm.getOrElse(false)

    }

    private def createBranch(branch: org.eclipse.egit.github.core.RepositoryBranch) = Branch(branch.getName, GithubApplication.url(branch.getName))

    private def parsePullRequest(pr: PR): PullRequest = PullRequest(pr.getHead.getRef, pr.getNumber, pr.getHtmlUrl, new DateTime(pr.getCreatedAt),
      PullRequestStatus(pr.isMergeable, pr.isMerged, isReviewed(pr.getId.toInt)))

    def mergePullRequest(prId: Int, user: User): MergeResult = {
      val pr = prService.getPullRequest(repo, prId)

      val description: String = s"Merged by ${user.fullName} (${user.githubLogin})\n${pr.getTitle}\n${pr.getBodyText}"
      val status = prService.merge(repo, prId, description)
      MergeResult(status.isMerged, status.getMessage, status.getSha)
    }

    def deleteBranch(branchName: String) = referenceService.deleteReference(repo, s"heads/$branchName")

    override def setStatus(ref: String, status: GithubStatus): Unit = {
      val commitStatus = new CommitStatus()
      commitStatus.setState(status.state)
      commitStatus.setTargetUrl(status.targetUrl)
      commitStatus.setDescription(status.description)

      commitService.createStatus(repo, ref, commitStatus)
    }
  }

}