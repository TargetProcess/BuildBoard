package models.buildActions

import models.cycles._

object BuildAction {
  def unapply(action: BuildAction) = {

    val (prId: Option[Int], branch: Option[String], cycle: Option[Cycle], cycleName: String) = action match {
      case a@PullRequestBuildAction(id, _) => (Some(id), None, Some(a.cycle), a.cycle.name)
      case a@BranchBuildAction(b, _) => (None, Some(b), Some(a.cycle), a.cycle.name)
      case a@ReuseArtifactsBuildAction(b, _, cycle) => (None, Some(b), Some(cycle), a.cycle.name)
      case DeployBuildAction(b, _, cycleName) => (None, Some(b), None, cycleName)
      case TransifexBuildAction(b) => (None, Some(b), None, "transifex")
    }

    val possibleBuildParameters = cycle.map(_.possibleBuildParameters).getOrElse(Nil)
    Some(action.name, prId, branch, cycleName, action.action, possibleBuildParameters)
  }
}

trait BuildAction {
  val branchName: String
  val name: String
  val action: String
}

