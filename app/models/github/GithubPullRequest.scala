package models.github

import org.eclipse.egit.github.core.{PullRequest => PR}
import org.joda.time.DateTime
import models.{PullRequestStatus, PullRequest}

object GithubPullRequest {
  def create(pr: PR): PullRequest = PullRequest(pr.getHead.getRef, pr.getNumber, pr.getHtmlUrl, new DateTime(pr.getCreatedAt), PullRequestStatus(pr.isMergeable, pr.isMerged))
}
