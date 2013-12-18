package models

import com.github.nscala_time.time.Imports._

case class Build(number: Int, branch: String, status: Option[String], url: String, timeStamp: DateTime, node: BuildNode)

case class BuildNode(number: Int, name: String, runName: String, status: Option[String], statusUrl: String, artifactsUrl: Option[String], timestamp: DateTime, children: List[BuildNode] = Nil)


trait Cycle {
  val unitTests = "All"
  val includeUnstable = false
  val funcTests: String
}

case object FullCycle extends Cycle {
  val funcTests = "All"

  override def toString = "Full"
}


case object ShortCycle extends Cycle {
  val funcTests = "\"PartComet PartViews1 PartViews2 PartViews3 PartViews4 PartViews5 PartViews6 PartViews0 PluginsPart1 PluginsPart2 PluginsPart3 PluginsPartOther PartPy1 PartPy2 PartBoard1\""

  override def toString = "Short"
}

trait BuildAction {
  val fullCycle: Boolean
  val cycle: Cycle = if (fullCycle) FullCycle else ShortCycle

  val branchName: String

  def parameters: List[(String, String)] = List(
    "BRANCHNAME" -> branchName,
    "Cycle" -> cycle.toString,
    "IncludeUnitTests" -> cycle.unitTests,
    "IncludeFuncTests" -> cycle.funcTests,
    "BuildFullPackage" -> "False",
    "INCLUDE_UNSTABLE" -> cycle.includeUnstable.toString
  )

  val name: String

}

object BuildAction {
  def unapply(action: BuildAction) = action match {
    case PullRequestBuildAction(id, _) => Some(action.name, Some(id), None, action.fullCycle)
    case BranchBuildAction(branch, _) => Some(action.name, None, Some(branch), action.fullCycle)
  }
}

case class PullRequestBuildAction(pullRequestId: Int, fullCycle: Boolean) extends BuildAction {
  val branchName: String = s"origin/pr/$pullRequestId/merge"

  val name = s"Build $cycle pull request"
}

case class BranchBuildAction(branch: String, fullCycle: Boolean) extends BuildAction {
  val branchName: String = s"origin/$branch"

  val name = s"Build $cycle branch"
}




