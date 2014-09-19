package components

import models._
import models.branches.Branch
import models.github.GithubStatus

trait GithubServiceComponent {
  val githubService: GithubService

  trait GithubService {
    def getBranches: List[Branch]

    def getPullRequests: List[PullRequest]

    def mergePullRequest(number: Int, user: User): MergeResult

    def deleteBranch(branchName: String)

    def getPullRequestStatus(id: Int): PullRequestStatus

    def setStatus(ref:String, status:GithubStatus)

  }

}