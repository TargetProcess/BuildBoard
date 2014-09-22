package models

import play.api.Play
import play.api.Play.current

import scala.collection.JavaConverters._
import scala.util.Try

object Cycle {
  val unitTestsCategoryName = "unitTests"
  val funcTestsCategoryName = "funcTests"
}

trait Cycle {
  val name: String

  def friendlyName = name

  val includeUnstable: Boolean
  val buildFullPackage: Boolean
  val unitTests: String
  val funcTests: String
  val includeComet: Boolean
  val includeSlice: Boolean
  val includeCasper: Boolean
  val includeDb: Boolean
}

case class CustomCycle(parameters: List[BuildParametersCategory]) extends Cycle {
  val sliceCategoryName = "SliceLoadTests"
  val cometCategoryName = "CometTests"
  val casperCategoryName = "CasperTests"
  val dbCategoryName = "DbTests"
  val cycleTypeCategoryName = "CycleType"

  override val name = "Custom"

  override def friendlyName = "custom parts"

  override val buildFullPackage = false
  override val includeUnstable: Boolean = false

  override val unitTests: String = getTestsByCategory(Cycle.unitTestsCategoryName)
  override val includeComet: Boolean = getBoolByCategory(cometCategoryName)
  override val funcTests: String = getTestsByCategory(Cycle.funcTestsCategoryName)
  override val includeSlice: Boolean = getBoolByCategory(sliceCategoryName)
  override val includeCasper: Boolean = getBoolByCategory(casperCategoryName)
  override val includeDb: Boolean = getBoolByCategory(dbCategoryName)
  val fullBuildCycle:Boolean = getBoolByCategory(cycleTypeCategoryName)

  def getBoolByCategory(categoryName: String): Boolean = {
    parameters.find(x => x.name == categoryName).exists(x => x.parts.nonEmpty)
  }

  def getTestsByCategory(categoryName: String): String = {
    parameters.find(x => x.name == categoryName).map(x => if (x.parts.isEmpty) "" else "\"" + x.parts.mkString(" ") + "\"").getOrElse("All")
  }
}

abstract class ConfigurableCycle(val name: String) extends Cycle {
  val config = Play.configuration.getConfig(s"build.cycle.$name").get
  val unitTests = getTests(Cycle.unitTestsCategoryName)
  val funcTests = getTests(Cycle.funcTestsCategoryName)

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
  override val includeCasper = false
  override val includeDb = false
}

case object FullCycle extends ConfigurableCycle("Full") {
  val includeComet = true
  val includeSlice = true
  override val includeCasper = true
  override val includeDb = true
}

case object ShortCycle extends ConfigurableCycle("Short") {
  val includeComet = false
  val includeSlice = false
  override val includeCasper = true
  override val includeDb = false
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
      case c@CustomCycle(_) => if (c.fullBuildCycle) "Full" else "Short"
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
    "INCLUDE_SLICE" -> cycle.includeSlice.toString,
    "INCLUDE_CASPER" -> cycle.includeCasper.toString,
    "INCLUDE_DB" -> cycle.includeDb.toString
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
    case customAction@PullRequestCustomBuildAction(pullRequestId, _) => Some(action.name, Some(pullRequestId), None, action.cycle.name, customAction.getPossibleBuildParameters)
  }
}

trait BranchBuildActionTrait extends BuildAction{
  val branch: String
  val branchName: String = s"origin/$branch"
  val name = s"Build ${cycle.friendlyName} on branch"
}

trait PullRequestBuildActionTrait extends BuildAction{
  val pullRequestId: Int
  val branchName: String = s"origin/pr/$pullRequestId/merge"
  val name = s"Build ${cycle.friendlyName} on pull request"
}

case class PullRequestBuildAction(pullRequestId: Int, cycle: Cycle) extends PullRequestBuildActionTrait {
}

case class BranchBuildAction(branch: String, cycle: Cycle) extends BranchBuildActionTrait {
}

case class BuildParametersCategory(name: String, parts: List[String]) {
}

case class PullRequestCustomBuildAction(pullRequestId: Int, cycle: CustomCycle) extends CustomBuildAction with PullRequestBuildActionTrait{

}

case class BranchCustomBuildAction(branch: String, cycle: CustomCycle) extends CustomBuildAction with BranchBuildActionTrait{
}

trait CustomBuildAction extends BuildAction {
  val cycle: CustomCycle
  def getPossibleBuildParameters: List[BuildParametersCategory] = {
    val cycleName = cycle.name
    val config = Play.configuration.getConfig(s"build.cycle.$cycleName").get
    List(
      BuildParametersCategory(cycle.cycleTypeCategoryName, List("build full package")),
      BuildParametersCategory(Cycle.unitTestsCategoryName, config.getStringList(Cycle.unitTestsCategoryName).get.asScala.toList.distinct),
      BuildParametersCategory(Cycle.funcTestsCategoryName, config.getStringList(Cycle.funcTestsCategoryName).get.asScala.toList.distinct),
      BuildParametersCategory(cycle.cometCategoryName, List("Include")),
      BuildParametersCategory(cycle.sliceCategoryName, List("Include")),
      BuildParametersCategory(cycle.casperCategoryName, List("Include")),
      BuildParametersCategory(cycle.dbCategoryName, List("Include"))
    )
  }
}