package models.buildActions

import models.cycles.{CustomCycle, Cycle}

case class TransifexBuildAction(buildName: String) extends JenkinsBuildAction {
  override val name = "Synchronize with Transifex"
  override val branchName: String = buildName
  override val cycle: Cycle = CustomCycle()
  override val jobName = "Transifex"
}
