package models.buildActions

import models.BranchInfo
import models.cycles.Cycle

trait JenkinsBuildAction extends BuildAction {
  val cycle: Cycle


  lazy val parameters: List[(String, String)] = List(
    "BRANCHNAME" -> branchName,
    "BUILDPRIORITY" -> (branchName match {
      case BranchInfo.hotfix(_) => "1"
      case BranchInfo.release(_) => "2"
      case BranchInfo.vs(_) => "3"
      case BranchInfo.develop() => "4"
      case BranchInfo.feature(_) => "5"
      case _ => "10"
    })
  ) ++ cycle.parameters
  override val action = "forceBuild"
  val jobName = "StartBuild"
}
