package components

import models._

trait GithubRepositoryComponent {
  val githubRepository: GithubRepository

  trait GithubRepository {
    def getBranches: List[Branch]

    def getPullRequests: List[PullRequest]

    def mergePullRequest(number: Int, user: User): MergeResult

    def deleteBranch(branchName: String)

    def getPullRequestStatus(id: Int): PullRequestStatus
  }

}