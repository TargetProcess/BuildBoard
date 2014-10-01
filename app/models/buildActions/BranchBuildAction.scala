package models.buildActions

import models.cycles.Cycle

case class BranchBuildAction(branch: String, cycle: Cycle) extends BranchBuildActionTrait
