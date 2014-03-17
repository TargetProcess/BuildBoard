package models.github

import models._

object GithubBranch {
  def create(branch:org.eclipse.egit.github.core.RepositoryBranch) = Branch(branch.getName, GithubApplication.url(branch.getName))
}

trait GithubRepository {
  def getBranches: List[Branch]
  def getPullRequests: List[PullRequest]
  def getPullRequestStatus(id: Int):PullRequestStatus
  def getUrlForBranch(name:String) =  GithubApplication.url(name)
  def getCommits(hashes: List[String]): List[Commit]
}