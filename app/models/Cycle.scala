package models

import play.api.Play
import play.api.Play.current

trait Cycle {
  val name: String
  def friendlyName = name
  val includeUnstable: Boolean
  val buildFullPackage: Boolean
  val unitTests: String
  val funcTests: String
}

abstract class ConfigurableCycle(val name: String) extends Cycle {
  val configPath = "build.cycle." + name

  val includeUnstable = Play.configuration.getBoolean(s"$configPath.includeUnstable").getOrElse(false)
  val unitTests = Play.configuration.getString(s"$configPath.unitTests").getOrElse("All")
  val funcTests = Play.configuration.getString(s"$configPath.funcTests").getOrElse("All")
  val buildFullPackage = Play.configuration.getBoolean(s"$configPath.buildFullPackage").getOrElse(false)
}

case object BuildPackageOnly extends ConfigurableCycle("PackageOnly") {
  override def friendlyName = "Package only"
}
case object FullCycle extends ConfigurableCycle("Full")
case object ShortCycle extends ConfigurableCycle("Short")

trait BuildAction {
  val release = "^(?:origin/)?release/(.*)$".r
  val feature = "^(?:origin/)?feature/(.*)$".r
  val hotfix = "^(?:origin/)?hotfix/(.*)$".r
  val vs = "^(?:origin/)?vs/(.*)$".r
  val develop = "^(?:origin/)?develop$".r


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
    "BUILDPRIORITY"->( branchName match {
      case hotfix(_)=>"1"
      case release(_)=>"2"
      case vs(_)=>"3"
      case develop()=>"4"
      case feature(_)=>"5"
      case _ => "10"
    })
  )

  val name: String
}

object BuildAction {
  val cycles = List(FullCycle, ShortCycle, BuildPackageOnly)

  def find(name:String) = cycles.find(_.name == name).get

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