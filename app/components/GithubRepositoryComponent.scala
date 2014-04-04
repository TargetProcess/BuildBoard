package components

import models._

trait GithubRepositoryComponent {
  def githubRepository: GithubRepository

  trait GithubRepository {
    def getBranches: List[Branch]

    def getPullRequests: List[PullRequest]

    def mergePullRequest(number: Int, user: User): MergeResult

    def deleteBranch(branchName: String)

    def getPullRequestStatus(id: Int): PullRequestStatus
  }

}