package models.buildActions


case class DeployBuildAction(buildName: String, buildNumber: Int, teamName: String) extends BuildAction {
  override val name = s"Deploy to $teamName"
  override val branchName: String = buildName
  override val action = "deployBuild"
}
