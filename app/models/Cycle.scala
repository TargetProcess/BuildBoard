package models

import play.api.Play
import play.api.Play.current

import scala.collection.JavaConverters._
import scala.util.Try

object Cycle {
  val unitTestsCategoryName = "unitTests"
  val funcTestsCategoryName = "funcTests"
  val sliceCategoryName = "SliceLoadTests"
  val cometCategoryName = "CometTests"
  val casperCategoryName = "CasperTests"
  val dbCategoryName = "DbTests"
  val cycleTypeCategoryName = "CycleType"
}

trait Cycle {
  lazy val parameters = {
    List("IncludeUnitTests" -> unitTests,
      "IncludeFuncTests" -> funcTests,
      "BuildFullPackage" -> buildFullPackage.toString,
      "INCLUDE_UNSTABLE" -> includeUnstable.toString,
      "Cycle" -> (if (isFull) "Full" else "Short"),
      "INCLUDE_COMET" -> includeComet.toString,
      "INCLUDE_SLICE" -> includeSlice.toString,
      "INCLUDE_CASPER" -> includeCasper.toString,
      "INCLUDE_DB" -> includeDb.toString)
  }


  val name: String

  val includeUnstable: Boolean
  val buildFullPackage: Boolean
  val unitTests: String
  val funcTests: String
  val includeComet: Boolean
  val includeSlice: Boolean
  val includeCasper: Boolean
  val includeDb: Boolean
  val isFull: Boolean
}

case class CustomCycle(buildParametersCategory: List[BuildParametersCategory]) extends Cycle {

  override val name = "Custom"

  override val buildFullPackage = false
  override val includeUnstable: Boolean = false

  override val unitTests: String = getTestsByCategory(Cycle.unitTestsCategoryName)
  override val includeComet: Boolean = getBoolByCategory(Cycle.cometCategoryName)
  override val funcTests: String = getTestsByCategory(Cycle.funcTestsCategoryName)
  override val includeSlice: Boolean = getBoolByCategory(Cycle.sliceCategoryName)
  override val includeCasper: Boolean = getBoolByCategory(Cycle.casperCategoryName)
  override val includeDb: Boolean = getBoolByCategory(Cycle.dbCategoryName)
  override val isFull: Boolean = getBoolByCategory(Cycle.cycleTypeCategoryName)

  def getBoolByCategory(categoryName: String): Boolean = {
    buildParametersCategory.find(x => x.name == categoryName).exists(x => x.parts.nonEmpty)
  }

  def getTestsByCategory(categoryName: String): String = {
    buildParametersCategory.find(x => x.name == categoryName).map(x => if (x.parts.isEmpty) "" else "\"" + x.parts.mkString(" ") + "\"").getOrElse("All")
  }
}

abstract class ConfigurableCycle(val name: String) extends Cycle {
  val config = Play.configuration.getConfig(s"build.cycle.$name").get

  val unitTests = getTests(Cycle.unitTestsCategoryName)
  val funcTests = getTests(Cycle.funcTestsCategoryName)
  val includeUnstable = getBoolean("includeUnstable")
  val buildFullPackage = getBoolean("buildFullPackage")
  val includeComet = getBoolean("includeComet")
  val includeSlice = getBoolean("includeSlice")
  val includeCasper = getBoolean("includeCasper")
  val includeDb = getBoolean("includeDb")
  val isFull = getBoolean("isFull")


  def getBoolean(path: String) = config.getBoolean(path).getOrElse(false)

  def getTests(path: String): String = {
    Try {
      config.getStringList(path).map(l => "\"" + l.asScala.mkString(" ") + "\"").get
    }.toOption
      .orElse(config.getString(path))
      .getOrElse("All")
  }
}

case object BuildPackageOnly extends ConfigurableCycle("Package only") {
}

case object FullCycle extends ConfigurableCycle("Full") {
}

case object ShortCycle extends ConfigurableCycle("Short") {

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

trait BranchBuildActionTrait extends BuildAction {
  val branch: String
  val branchName: String = s"origin/$branch"
  val name = s"Build ${cycle.name} on branch"
}

trait PullRequestBuildActionTrait extends BuildAction {
  val pullRequestId: Int
  val branchName: String = s"origin/pr/$pullRequestId/merge"
  val name = s"Build ${cycle.name} on pull request"
}

case class PullRequestBuildAction(pullRequestId: Int, cycle: Cycle) extends PullRequestBuildActionTrait

case class BranchBuildAction(branch: String, cycle: Cycle) extends BranchBuildActionTrait

case class BuildParametersCategory(name: String, parts: List[String])

case class PullRequestCustomBuildAction(pullRequestId: Int, cycle: CustomCycle) extends CustomBuildAction with PullRequestBuildActionTrait

case class BranchCustomBuildAction(branch: String, cycle: CustomCycle) extends CustomBuildAction with BranchBuildActionTrait

trait CustomBuildAction extends BuildAction {
  val cycle: CustomCycle

  def getPossibleBuildParameters: List[BuildParametersCategory] = {
    val cycleName = cycle.name
    val config = Play.configuration.getConfig(s"build.cycle.$cycleName").get
    List(
      BuildParametersCategory(Cycle.cycleTypeCategoryName, List("build full package")),
      BuildParametersCategory(Cycle.unitTestsCategoryName, config.getStringList(Cycle.unitTestsCategoryName).get.asScala.toList.distinct),
      BuildParametersCategory(Cycle.funcTestsCategoryName, config.getStringList(Cycle.funcTestsCategoryName).get.asScala.toList.distinct),
      BuildParametersCategory(Cycle.cometCategoryName, List("Include")),
      BuildParametersCategory(Cycle.sliceCategoryName, List("Include")),
      BuildParametersCategory(Cycle.casperCategoryName, List("Include")),
      BuildParametersCategory(Cycle.dbCategoryName, List("Include"))
    )
  }
}