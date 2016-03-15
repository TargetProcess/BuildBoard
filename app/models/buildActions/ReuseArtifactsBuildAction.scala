package models.buildActions

import models.cycles.Cycle


case class ReuseArtifactsBuildAction(buildName: String, buildNumber: Int, cycle: Cycle) extends CycleAwareJenkinsBuildAction {
  override val name = s"Build ${cycle.name} on this artifacts"
  override val branchName: String = buildName
}

