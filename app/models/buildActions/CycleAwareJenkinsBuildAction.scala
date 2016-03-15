package models.buildActions

import models.cycles.Cycle

trait CycleAwareJenkinsBuildAction extends JenkinsBuildAction {
  val cycle: Cycle
  override def parameters = super.parameters ++ cycle.parameters
  override val jobName = "StartBuild"
}
