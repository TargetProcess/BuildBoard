package models.buildActions

import models.cycles.CustomCycle

case class BranchCustomBuildAction(branch: String, cycle: CustomCycle) extends CustomBuildAction with BranchBuildActionTrait
