package models.buildActions

import models.cycles.{Cycle, CustomCycle}

case class DeployBuildAction(buildName: String, buildNumber: Int, stagingDirectoryName: String) extends BuildAction {
  override val name = s"Deploy $buildName to $stagingDirectoryName"
  override val branchName: String = buildName
  override val action = "deployBuild"
}
