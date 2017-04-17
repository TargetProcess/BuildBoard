package models

import com.github.nscala_time.time.Imports._


case class Build(number: Int,
                 branch: String,
                 status: Option[String],
                 timestamp: DateTime,
                 timestampEnd: Option[DateTime],
                 toggled: Boolean = false,
                 commits: List[Commit] = Nil,
                 ref: Option[String] = None,
                 initiator: Option[String] = None,
                 description: Option[String] = None,
                 pullRequestId: Option[Int] = None,
                 name: String,
                 artifacts: List[Artifact] = Nil,
                 activityType: String = "build",
                 node: Option[BuildNode],
                 pendingReruns: List[String] = List()
                ) extends ActivityEntry {


  def getTestRunBuildNode(part: String, run: String): Option[BuildNode] = {
    getBuildNode(n => n.name == part && n.runName == run)
  }

  def getBuildNode(filter: BuildNode => Boolean): Option[BuildNode] = {
    def getTestRunBuildNodeInner(node: BuildNode): Option[BuildNode] = node match {
      case n: BuildNode if filter(n) => Some(n)
      case n => n.children.map(getTestRunBuildNodeInner).filter(_.isDefined).flatten.headOption
    }

    node.flatMap(getTestRunBuildNodeInner)
  }

  def isPullRequest = pullRequestId.isDefined

  val selfBuildStatus = BuildStatus(status, toggled)

  val buildStatus = {
    val selfStatus = selfBuildStatus
    if (selfStatus == BuildStatus.Toggled || selfStatus == BuildStatus.InProgress)
      selfStatus
    else
      node.map(_.buildStatus).getOrElse(selfStatus)
  }
}

case class BuildNode(
                      id: String,
                      name: String,
                      runName: String,
                      number: Int,
                      status: Option[String],
                      statusUrl: String,
                      artifacts: List[Artifact],
                      timestamp: DateTime,
                      timestampEnd: Option[DateTime],
                      rerun: Option[Boolean],
                      isUnstable: Option[Boolean] = None,
                      children: List[BuildNode] = Nil,
                      testResults: List[TestCasePackage] = Nil
                    ) {

  def allChildren: Stream[BuildNode] = {
    children.toStream.flatMap(x => x #:: x.allChildren)
  }

  def getTestCase(name: String): Option[TestCase] = {
    def getTestCaseInner(tcPackage: TestCasePackage): Option[TestCase] = {
      tcPackage.testCases.find(tc => tc.name == name).orElse(getTestCasesInner(tcPackage.packages))
    }

    def getTestCasesInner(packages: List[TestCasePackage]): Option[TestCase] = packages
      .map(getTestCaseInner)
      .filter(tc => tc.isDefined)
      .flatten
      .headOption

    getTestCasesInner(testResults)
  }

  val buildStatus: BuildStatusBase = BuildStatus.getBuildStatus(status, children)
}

case class BuildToggle(branch: String, buildNumber: Int)

case class Artifact(name: String, url: String)

case class JobRun(
                   id: String,
                   buildNumber: Int,
                   BuildNode: String,
                   FailedTests: List[TestCase],
                   startTime: DateTime,
                   endTime: Option[DateTime])
