package models.buildActions

import models.cycles.Cycle

case class PullRequestBuildAction(pullRequestId: Int, cycle: Cycle) extends PullRequestBuildActionTrait
