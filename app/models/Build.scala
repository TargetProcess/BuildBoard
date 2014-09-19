package models

import com.github.nscala_time.time.Imports._



case class Build(number: Int,
                 branch: String,
                 status: Option[String],
                 timestamp: DateTime,
                 toggled: Boolean = false,
                 commits: List[Commit] = Nil,
                 ref: Option[String] = None,
                 initiator: Option[String] = None,
                 pullRequestId: Option[Int] = None,
                 name: String,
                 activityType: String = "build",
                 node: Option[BuildNode]
                  ) extends ActivityEntry {

  def getTestRunBuildNode(part: String, run: String): Option[BuildNode] = {
    def getTestRunBuildNodeInner(node: BuildNode): Option[BuildNode] = node match {
      case n: BuildNode if n.name == part && n.runName == run => Some(n)
      case n => n.children.map(getTestRunBuildNodeInner).filter(_.isDefined).flatten.headOption
    }
    node.map(getTestRunBuildNodeInner).flatten
  }

  val buildStatus = {
    val selfStatus = BuildStatus(status, toggled)
    if (selfStatus == BuildStatus.Toggled)
      selfStatus
    else
      node.map(_.buildStatus).getOrElse(selfStatus)
  }

  def isPullRequest = pullRequestId.isDefined
}


case class BuildNode(
                      name: String,
                      runName: String,
                      status: Option[String],
                      statusUrl: String,
                      artifacts: List[Artifact],
                      timestamp: DateTime,
                      children: List[BuildNode] = Nil,
                      testResults: List[TestCasePackage] = Nil
                      )
{
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

  val buildStatus:BuildStatusBase = BuildStatus.getBuildStatus(status, children)
}

case class BuildToggle(branch: String, buildNumber: Int)

case class Artifact(name: String, url: String)
