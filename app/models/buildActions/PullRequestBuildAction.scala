package models.buildActions

import models.cycles.Cycle

case class PullRequestBuildAction(pullRequestId: Int, cycle: Cycle)  extends BuildAction {
  val branchName: String = s"origin/pr/$pullRequestId/merge"
  val name = s"Build ${cycle.name} on pull request"
}
