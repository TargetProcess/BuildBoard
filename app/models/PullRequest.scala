package models

import org.eclipse.egit.github.core.{PullRequest=>PR}

case class PullRequest(id: Int, url: String) {}

case class PullRequestStatus(isMergeable:Boolean, isMerged:Boolean)

object PullRequest{
  def apply(pr:PR):PullRequest = PullRequest(pr.getNumber, pr.getHtmlUrl)
}
