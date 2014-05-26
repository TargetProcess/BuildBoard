package models

import com.github.nscala_time.time.Imports._
import scala.Some


trait IBuildInfo {
  val number: Int
  val branch: String
  val status: Option[String]
  val toggled: Boolean
  val timestamp: DateTime
  val pullRequestId: Option[Int]
  val name: String

  val buildStatus = BuildStatus(status, toggled)

  def isPullRequest = pullRequestId.isDefined

  val initiator:Option[String]
}

case class BuildInfo(number: Int,
                     branch: String,
                     status: Option[String],
                     override val timestamp: DateTime,
                     toggled: Boolean = false,
                     commits: List[Commit] = Nil,
                     pullRequestId: Option[Int] = None,
                     initiator:Option[String] = None,
                     name: String,
                     activityType: String = "build"
                      ) extends ActivityEntry with IBuildInfo {

}

case class Build(number: Int,
                 branch: String,
                 status: Option[String],
                 timestamp: DateTime,
                 toggled: Boolean = false,
                 commits: List[Commit] = Nil,
                 initiator:Option[String] = None,
                 pullRequestId: Option[Int] = None,
                 name: String,
                 node: Option[BuildNode]) extends IBuildInfo {
  def getTestRunBuildNode(part: String, run: String): Option[BuildNode] = {
    def getTestRunBuildNodeInner(node: BuildNode): Option[BuildNode] = node match {
      case n: BuildNode if n.name == part && n.runName == run => Some(n)
      case n => n.children.map(getTestRunBuildNodeInner).filter(_.isDefined).flatten.headOption
    }
    node.map(getTestRunBuildNodeInner).flatten
  }

  def getLeafNodes:List[BuildNode] = node.map(_.getLeafNodes).getOrElse(Nil)




}

object BuildImplicits {
  implicit def toBuildInfo(b: Build): BuildInfo = BuildInfo(b.number, b.branch, b.status, b.timestamp, b.toggled, b.commits, b.pullRequestId, b.initiator, b.name)
}


case class BuildNode(name: String, runName: String, status: Option[String], statusUrl: String, artifacts: List[Artifact], timestamp: DateTime, children: List[BuildNode] = Nil, testResults: List[TestCasePackage] = Nil) {
  def getLeafNodes: List[BuildNode] = {
    if (children.isEmpty) List(this)
    else children.flatMap(_.getLeafNodes)
  }

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
