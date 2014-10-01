package models.buildActions

import models.cycles.CustomCycle

case class PullRequestCustomBuildAction(pullRequestId: Int, cycle: CustomCycle) extends CustomBuildAction with PullRequestBuildActionTrait
