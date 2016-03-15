package models.buildActions

case class TransifexBuildAction(buildName: String) extends JenkinsBuildAction {
  override val name = "Synchronize with Transifex"
  override val branchName: String = buildName
  override val action = "forceBuild"
  override val jobName = "Transifex"
}
