package models.buildActions

trait PullRequestBuildActionTrait extends BuildAction {
  val pullRequestId: Int
  val branchName: String = s"origin/pr/$pullRequestId/merge"
  val name = s"Build ${cycle.name} on pull request"
}
