package models.buildActions

import models.cycles.{Cycle, CustomCycle}

case class ReuseArtifactsBuildAction(buildName: String, buildNumber: Int, cycle: Cycle = CustomCycle()) extends JenkinsBuildAction {
  override val name = s"Build ${cycle.name} on this artifacts"
  override val branchName: String = buildName
}

