package models.buildActions

import models.cycles.CustomCycle

trait CustomBuildAction extends BuildAction {
  val cycle: CustomCycle
}
