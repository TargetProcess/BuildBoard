package models.buildActions

import models._
import models.cycles._

object BuildAction {
  val branchCycles = List(FullCycle, ShortCycle, PackageOnlyCycle)

  def find(name: String) = branchCycles.find(_.name == name).get

  def unapply(action: BuildAction) = {

    val (prId:Option[Int], branch:Option[String], cycle:Option[Cycle]) = action match {
      case a@PullRequestBuildAction(id, _) => (Some(id), None, Some(a.cycle))
      case a@BranchBuildAction(b, _) => (None, Some(b), Some(a.cycle))
      case a@ReuseArtifactsBuildAction(b, _, cycle) => (None, Some(b), Some(cycle))
      case DeployBuildAction(b, _, _) => (None, Some(b), None)
    }

    val possibleBuildParameters = cycle.map {
      case c@CustomCycle(_) => c.getPossibleBuildParameters
      case _ => Nil
    }.getOrElse(Nil)


    Some(action.name, prId, branch, cycle.map(_.name).getOrElse(""), action.action, possibleBuildParameters)
  }
}

trait BuildAction {
  val branchName: String
  val name: String
  val action: String
}

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
}