package models.buildActions

import models.BranchInfo
import models.cycles.Cycle

trait JenkinsBuildAction extends BuildAction {
  def parameters: List[(String, String)] = List(
    "BRANCHNAME" -> branchName,
    "BUILDPRIORITY" -> (branchName match {
      case BranchInfo.hotfix(_) => "1"
      case BranchInfo.release(_) => "2"
      case BranchInfo.vs(_) => "3"
      case BranchInfo.develop() => "4"
      case BranchInfo.feature(_) => "5"
      case _ => "10"
    })
  )
  override val action = "forceBuild"
  val jobName: String
}




