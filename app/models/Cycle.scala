package models

import java.util

import play.api.Play
import play.api.Play.current
import play.api.libs.json.Reads
import play.api.mvc._
import scala.collection.JavaConverters._
import scala.util.Try
import play.api.libs.functional.syntax._
import play.api.libs.json._

trait Cycle {
  val name: String

  def friendlyName = name

  val includeUnstable: Boolean
  val buildFullPackage: Boolean
  val unitTests: String
  val funcTests: String
  val includeComet: Boolean
  val includeSlice: Boolean
}

case class CustomCycle(parameters: List[BuildParametersCategory]) extends Cycle {
  override val name = "Custom"

  override def friendlyName = "custom parts"

  override val buildFullPackage = false
  override val includeUnstable: Boolean = false

  override val unitTests: String = getTestsByCategory("unitTests")
  override val includeComet: Boolean = false
  override val funcTests: String = getTestsByCategory("unitTests")
  override val includeSlice: Boolean = false

  def getTestsByCategory(categoryName:String): String = {
    parameters.find(x => x.name == categoryName).map(x => "\"" + x.parts.mkString(" ") + "\"").getOrElse("All")
  }
}

abstract class ConfigurableCycle(val name: String) extends Cycle {
  val config = Play.configuration.getConfig(s"build.cycle.$name").get
  val unitTests = getTests("unitTests")
  val funcTests = getTests("funcTests")

  val includeUnstable = config.getBoolean("includeUnstable").getOrElse(false)
  val buildFullPackage = config.getBoolean("buildFullPackage").getOrElse(false)

  def getTests(path: String): String = {
    Try {
      config.getStringList(path).map(l => "\"" + l.asScala.mkString(" ") + "\"").get
    }.toOption
      .orElse(config.getString(path))
      .getOrElse("All")
  }
}

case object BuildPackageOnly extends ConfigurableCycle("PackageOnly") {
  override def friendlyName = "Package only"

  val includeComet = false
  val includeSlice = false
}

case object FullCycle extends ConfigurableCycle("Full") {
  val includeComet = true
  val includeSlice = true
}

case object ShortCycle extends ConfigurableCycle("Short") {
  val includeComet = false
  val includeSlice = false
}

trait BuildAction {
  val cycle: Cycle

  val branchName: String

  lazy val parameters: List[(String, String)] = List(
    "BRANCHNAME" -> branchName,
    "IncludeUnitTests" -> cycle.unitTests,
    "IncludeFuncTests" -> cycle.funcTests,
    "BuildFullPackage" -> (if (cycle.buildFullPackage) "true" else "false"),
    "INCLUDE_UNSTABLE" -> (if (cycle.includeUnstable) "true" else "false"),
    "Cycle" -> (cycle match {
      case FullCycle => "Full"
      case ShortCycle => "Short"
      case BuildPackageOnly => "Short"
      case CustomCycle(_) => "Short"
    }),
    "BUILDPRIORITY" -> (branchName match {
      case BranchInfo.hotfix(_) => "1"
      case BranchInfo.release(_) => "2"
      case BranchInfo.vs(_) => "3"
      case BranchInfo.develop() => "4"
      case BranchInfo.feature(_) => "5"
      case _ => "10"
    }),
    "INCLUDE_COMET" -> cycle.includeComet.toString,
    "INCLUDE_SLICE" -> cycle.includeSlice.toString
  )

  val name: String
}

object BuildAction {
  val cycles = List(FullCycle, ShortCycle, BuildPackageOnly)

  def find(name: String) = cycles.find(_.name == name).get

  def unapply(action: BuildAction) = action match {
    case PullRequestBuildAction(id, _) => Some(action.name, Some(id), None, action.cycle.name, List[BuildParametersCategory]())
    case BranchBuildAction(branch, _) => Some(action.name, None, Some(branch), action.cycle.name, List[BuildParametersCategory]())
    case customAction@BranchCustomBuildAction(branch, _) => Some(action.name, None, Some(branch), action.cycle.name, customAction.getPossibleBuildParameters)
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

case class BuildParametersCategory(name: String, parts: List[String]) {
}

case class BranchCustomBuildAction(branch: String, cycle: CustomCycle) extends BuildAction {

  def getPossibleBuildParameters: List[BuildParametersCategory] = {
    val cycleName = cycle.name
    val config = Play.configuration.getConfig(s"build.cycle.$cycleName").get
    List(
      BuildParametersCategory("UnitTests", config.getStringList("unitTests").get.asScala.toList.distinct),
      BuildParametersCategory("FuncTests", config.getStringList("funcTests").get.asScala.toList.distinct)
    )
  }

  val branchName: String = s"origin/$branch"
  val name = s"Build ${cycle.friendlyName} on branch"
}