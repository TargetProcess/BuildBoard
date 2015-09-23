package components

import models._
import models.github.GithubStatus

trait GithubServiceComponent {
  val githubService: GithubService

  trait GithubService {
    def getBranches: List[Branch]

    def getPullRequests: List[PullRequest]

    def mergePullRequest(number: Int, user: User, description:Option[String]=None): MergeResult

    def deleteBranch(branchName: String)

    def getPullRequestStatus(id: Int): PullRequestStatus

    def setStatus(ref:String, status:GithubStatus)

  }

}