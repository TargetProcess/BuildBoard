package models.github

import models._
import org.eclipse.egit.github.core.RepositoryBranch

case class GithubBranch(name:String)

object GithubBranch {
  def create(branch:org.eclipse.egit.github.core.RepositoryBranch) = GithubBranch(branch.getName)
}

trait GithubRepository {
  def getBranches: List[GithubBranch]
  def getPullRequests: List[PullRequest]
  def getPullRequestStatus(id: Int):PullRequestStatus
  def getUrlForBranch(name:String) =  GithubApplication.url(name)
}


