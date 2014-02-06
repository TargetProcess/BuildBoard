package models

import com.github.nscala_time.time.Imports._
import scala.Some
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import mongoContext._
import play.api.Play
import play.api.Play.current

case class Artifact(name: String, url: String)

case class Build(number: Int, branch: String, status: Option[String], url: String, timeStamp: DateTime, node: BuildNode, toggled: Boolean = false) {
  def getTestRunBuildNode(part: String, run: String): Option[BuildNode] = {
    def getTestRunBuildNodeInner(node: BuildNode): Option[BuildNode] = node match {
      case n: BuildNode if (n.name == part && n.runName == run) => Some(n)
      case n => n.children.map(getTestRunBuildNodeInner(_)).filter(_.isDefined).flatten.headOption
    }
    getTestRunBuildNodeInner(node)
  }
}

case class BuildNode(name: String, runName: String, status: Option[String], statusUrl: String, artifacts: List[Artifact], timestamp: DateTime, children: List[BuildNode] = Nil, testResults: List[TestCasePackage] = Nil)

case class BuildToggle(branch: String, buildNumber: Int)

object BuildToggles extends ModelCompanion[BuildToggle, ObjectId] with Collection[BuildToggle] {
  def collection = mongoCollection("buildToggles")

  val dao = new SalatDAO[BuildToggle, ObjectId](collection) {}

  collection.ensureIndex(DBObject("buildNumber" -> 1, "branch" -> 1), "build_number", unique = true)
}

object Builds extends ModelCompanion[Build, ObjectId] with Collection[Build] {
  def collection = mongoCollection("builds")

  val dao = new SalatDAO[Build, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("number" -> 1, "branch" -> 1), "build_number", unique = true)
}

trait Cycle {
  val name: String
  val includeUnstable: Boolean
  val buildFullPackage: Boolean
  val unitTests: String
  val funcTests: String
}

abstract class ConfigurableCycle(val name: String) extends Cycle {
  val configPath = "build.cycle." + name.toLowerCase()


  val includeUnstable = Play.configuration.getBoolean(s"$configPath.includeUnstable").getOrElse(false)
  val unitTests = Play.configuration.getString(s"$configPath.unitTests").getOrElse("All")
  val funcTests = Play.configuration.getString(s"$configPath.funcTests").getOrElse("All")
  val buildFullPackage = Play.configuration.getBoolean(s"$configPath.buildFullCycle").getOrElse(false)

}

case object FullCycle extends ConfigurableCycle("Full")

case object ShortCycle extends ConfigurableCycle("Short")

trait BuildAction {
  val fullCycle: Boolean
  val cycle: Cycle = if (fullCycle) FullCycle else ShortCycle

  val branchName: String

  def parameters: List[(String, String)] = List(
    "BRANCHNAME" -> branchName,
    "Cycle" -> cycle.name,
    "IncludeUnitTests" -> cycle.unitTests,
    "IncludeFuncTests" -> cycle.funcTests,
    "BuildFullPackage" -> cycle.buildFullPackage.toString,
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

  val name = s"Build ${cycle.name} pull request"
}

case class BranchBuildAction(branch: String, fullCycle: Boolean) extends BuildAction {
  val branchName: String = s"origin/$branch"

  val name = s"Build ${cycle.name} branch"
}