package models.buildActions

trait BranchBuildActionTrait extends BuildAction {
  val branch: String
  val branchName: String = s"origin/$branch"
  val name = s"Build ${cycle.name} on branch"
}

case class BranchBuildAction(branch: String, cycle: Cycle) extends BranchBuildActionTrait

trait PullRequestBuildActionTrait extends BuildAction {
  val pullRequestId: Int
  val branchName: String = s"origin/pr/$pullRequestId/merge"
  val name = s"Build ${cycle.name} on pull request"
}

case class PullRequestBuildAction(pullRequestId: Int, cycle: Cycle) extends PullRequestBuildActionTrait