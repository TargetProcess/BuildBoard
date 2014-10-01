package models.buildActions

trait BranchBuildActionTrait extends BuildAction {
  val branch: String
  val branchName: String = s"origin/$branch"
  val name = s"Build ${cycle.name} on branch"
}
