package models

import com.github.nscala_time.time.Imports._
import scala.Some
import com.novus.salat.annotations.raw.Salat

@Salat
trait BuildBase[TBuild <: BuildBase[TBuild]] extends ActivityEntry {
  val number: Int
  val timestamp: DateTime
}

case class BuildInfo(override val number: Int,
                     branch: String,
                     status: Option[String],
                     override val timestamp: DateTime,
                     isPullRequest: Boolean = false,
                     var toggled: Boolean = false,
                     commits: List[Commit] = Nil,
                     activityType:String = "build"
                      ) extends BuildBase[BuildInfo]

case class Build(
                  override val number: Int,
                  branch: String,
                  timestamp: DateTime,
                  node: BuildNode,
                  activityType:String = "build") extends BuildBase[Build] {

  def getTestRunBuildNode(part: String, run: String): Option[BuildNode] = {
    def getTestRunBuildNodeInner(node: BuildNode): Option[BuildNode] = node match {
      case n: BuildNode if n.name == part && n.runName == run => Some(n)
      case n => n.children.map(getTestRunBuildNodeInner).filter(_.isDefined).flatten.headOption
    }
    getTestRunBuildNodeInner(node)
  }

}

case class BuildNode(name: String, runName: String, status: Option[String], statusUrl: String, artifacts: List[Artifact], timestamp: DateTime, children: List[BuildNode] = Nil, testResults: List[TestCasePackage] = Nil) {
  def getTestCase(name: String): Option[TestCase] = {
    def getTestCaseInner(tcPackage: TestCasePackage): Option[TestCase] = {
      tcPackage.testCases.filter(tc => tc.name == name) match {
        case tc :: _ => Some(tc)
        case Nil => getTestCasesInner(tcPackage.packages)
      }
    }
    def getTestCasesInner(packages: List[TestCasePackage]): Option[TestCase] = packages
      .map(getTestCaseInner)
      .filter(tc => tc.isDefined)
      .flatten
      .headOption

    getTestCasesInner(testResults)
  }
}

case class BuildToggle(branch: String, buildNumber: Int)

case class Artifact(name: String, url: String)
