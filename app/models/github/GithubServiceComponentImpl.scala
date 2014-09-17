package models.github

import components._
import models._
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service._
import scala.collection.JavaConverters._
import org.joda.time.DateTime
import org.eclipse.egit.github.core.{PullRequest => PR, CommitStatus, RepositoryId}
import models.{PullRequestStatus, PullRequest}

trait GithubServiceComponentImpl extends GithubServiceComponent {
  this: GithubServiceComponentImpl
    with AuthInfoProviderComponent
  =>

  val githubService = new GithubServiceImpl(authInfo)

  class GithubServiceImpl(authInfo: AuthInfo) extends GithubService {
    private val github = new GitHubClient().setOAuth2Token(authInfo.githubToken)
    private val repositoryService = new RepositoryService(github)
    private val prService = new PullRequestService(github)
    private val repo = new RepositoryId(GithubApplication.user, GithubApplication.repo)
    private val referenceService = new ReferenceService(github)
    private val commitService = new CommitService(github)

    def getBranches: List[Branch] = repositoryService.getBranches(repo).asScala.map(b=>createBranch(b)).toList

    def getPullRequests: List[PullRequest] = prService.getPullRequests(repo, "open").asScala.map(createPullRequest).toList

    def getPullRequestStatus(id: Int) = {
      val pr = prService.getPullRequest(repo, id)
      PullRequestStatus(pr.isMergeable, pr.isMerged)
    }

    private def createBranch(branch: org.eclipse.egit.github.core.RepositoryBranch) =
      Branch(branch.getName, GithubApplication.url(branch.getName))

    private def createPullRequest(pr: PR): PullRequest =
      PullRequest(pr.getHead.getRef, pr.getNumber, pr.getHtmlUrl, new DateTime(pr.getCreatedAt), PullRequestStatus(pr.isMergeable, pr.isMerged))

    def mergePullRequest(number: Int, user: User): MergeResult = {
      val status = prService.merge(repo, number, s"Merged by ${user.fullName} (${user.githubLogin})")
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




