package models.buildActions

import models._
import models.cycles._

object BuildAction {
  val branchCycles = List(FullCycle, ShortCycle, PackageOnlyCycle)

  def find(name: String) = branchCycles.find(_.name == name).get

  def unapply(action: BuildAction) = {

    val (prId, branch) = action match {
      case PullRequestBuildAction(id, _) => (Some(id), None)
      case BranchBuildAction(b, _) => (None, Some(b))
      case ReuseArtifactsBuildAction(b, _, _) => (None, Some(b))
    }

    val possibleBuildParameters = action.cycle match {
      case c@CustomCycle(_) => c.getPossibleBuildParameters
      case _ => Nil
    }

    Some(action.name, prId, branch, action.cycle.name, possibleBuildParameters)
  }
}

trait BuildAction {
  val cycle: Cycle

  val branchName: String

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

  val name: String
}