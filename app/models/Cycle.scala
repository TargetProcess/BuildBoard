package models

import play.api.Play
import play.api.Play.current
import scala.collection.JavaConverters._
import scala.util.Try

trait Cycle {
  val name: String

  def friendlyName = name

  val includeUnstable: Boolean
  val buildFullPackage: Boolean
  val unitTests: String
  val funcTests: String
}

abstract class ConfigurableCycle(val name: String) extends Cycle {
  val config = Play.configuration.getConfig(s"build.cycle.$name").get
  val unitTests = getTests("unitTests")
  val funcTests = getTests("funcTests")

  val includeUnstable = config.getBoolean("includeUnstable").getOrElse(false)
  val buildFullPackage = config.getBoolean("buildFullPackage").getOrElse(false)

  def getTests(path: String): String = {
    Try{config.getStringList(path).map(l=>"\""+l.asScala.mkString(" ")+"\"").get}.toOption
      .orElse(config.getString(path))
      .getOrElse("All")
  }
}

case object BuildPackageOnly extends ConfigurableCycle("PackageOnly") {
  override def friendlyName = "Package only"
}

case object FullCycle extends ConfigurableCycle("Full")

case object ShortCycle extends ConfigurableCycle("Short")

trait BuildAction {
  val cycle: Cycle

  val branchName: String

  def parameters: List[(String, String)] = List(
    "BRANCHNAME" -> branchName,
    "IncludeUnitTests" -> cycle.unitTests,
    "IncludeFuncTests" -> cycle.funcTests,
    "BuildFullPackage" -> (if (cycle.buildFullPackage) "true" else "false"),
    "INCLUDE_UNSTABLE" -> (if (cycle.includeUnstable) "true" else "false"),
    "Cycle" -> (cycle match {
      case FullCycle => "Full"
      case ShortCycle => "Short"
      case BuildPackageOnly => "Short"
    }),
    "BUILDPRIORITY" -> (branchName match {
      case BranchInfo.hotfix(_) => "1"
      case BranchInfo.release(_) => "2"
      case BranchInfo.vs(_) => "3"
      case BranchInfo.develop() => "4"
      case BranchInfo.feature(_) => "5"
      case _ => "10"
    }),
    "INCLUDE_COMET" -> (cycle match {
      case FullCycle => "true"
      case ShortCycle => "false"
      case BuildPackageOnly => "false"
    }),
    "INCLUDE_SLICE" -> (cycle match {
      case FullCycle => "true"
      case ShortCycle => "false"
      case BuildPackageOnly => "false"
    })
  )

  val name: String
}

object BuildAction {
  val cycles = List(FullCycle, ShortCycle, BuildPackageOnly)

  def find(name: String) = cycles.find(_.name == name).get

  def unapply(action: BuildAction) = action match {
    case PullRequestBuildAction(id, _) => Some(action.name, Some(id), None, action.cycle.name)
    case BranchBuildAction(branch, _) => Some(action.name, None, Some(branch), action.cycle.name)
  }
}

case class PullRequestBuildAction(pullRequestId: Int, cycle: Cycle) extends BuildAction {
  val branchName: String = s"origin/pr/$pullRequestId/merge"

  val name = s"Build ${cycle.friendlyName} on pull request"
}

case class BranchBuildAction(branch: String, cycle: Cycle) extends BuildAction {
  val branchName: String = s"origin/$branch"

  val name = s"Build ${cycle.friendlyName} on branch"
}