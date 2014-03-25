package models.jenkins

import scala.util.Try
import scalaj.http.Http
import play.api.Play
import models._
import java.io.File
import scala.io.{BufferedSource, Source}
import play.api.Play.current
import scala.xml.{Node, XML}
import models.BuildNode
import scala.Some
import models.TestCase
import models.Build
import models.TestCasePackage
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._

trait JenkinsApi {
  self: JenkinsRepository =>

  private val jenkinsUrl = Play.configuration.getString("jenkins.url").get
  protected val rootJobName = "StartBuild"

  def forceBuild(action: models.BuildAction) = Try {
    Http.post(s"$jenkinsUrl/job/$rootJobName/buildWithParameters")
      .params(action.parameters)
      .asString
  }
}

trait FileApi {
  self: JenkinsRepository =>

  val directory = Play.configuration.getString("jenkins.data.path").get

  def read(f: File): Option[String] = Try {
    val file: BufferedSource = Source.fromFile(f)
    val result = file.mkString
    file.close()

    result
  }.toOption
}

trait Artifacts {
  self: JenkinsRepository =>

  protected val screenshot = "screenshot"
  protected val testNameRegex = """.*\.(\w+)\.(\w+)$""".r
  protected val screenshotQualifiedFileNameRegex = """.*\\(\w+)\.(\w+)[-].*$""".r
  protected val screenshotFileNameRegex = """.*\\(\w+)[-].*$""".r

  def getArtifacts(contents: List[File]): List[Artifact] = {
    def getArtifactsInner(file: File, filter: File => Boolean, artifactName: String): List[Artifact] = file.listFiles
      .filter(filter)
      .map(_.getPath.substring(directory.length + 1))
      .map(Artifact(artifactName, _))
      .toList

    contents.map(file => file.getName match {
      case ".TestResults" => getArtifactsInner(file, f => f.getName.endsWith(".xml"), "testResults")
      case ".Logs" => getArtifactsInner(file, f => f.getName.startsWith("SessionLogs"), "logs")
      case ".Screenshots" => getArtifactsInner(file, _ => true, screenshot)
      case _ => List()
    })
      .flatten
  }

  def getArtifact(file: String): File = new File(directory, file)

  def getAttribute(n: Node, key: String): Option[String] = n.attribute(key).map(_.headOption.map(_.text)).flatten
}

class JenkinsRepository extends JenkinsApi with FileApi with Artifacts {
  private val timeout = 5.hours
  private val buildsPerBranch = 15

  private case class BuildSource(branch: Branch, number: Int, pullRequestId: Option[Int], file: File)

  def getBuilds(branch: models.Branch): List[Build] = getBuildsSources(branch)
    .map(getBuild)
    .flatten
    .toList

  def getBuild(branch: models.Branch, number: Int): Option[Build] = {
    getBuildsSources(branch)
      .filter(_.number == number)
      .map(getBuild)
      .flatten
      .headOption
  }

  def getTestCasePackages(testRunBuildNode: BuildNode): List[TestCasePackage] = {
    val screenshots = testRunBuildNode.artifacts.filter(a => a.name == screenshot)

    def getTestCasePackage(node: Node): TestCasePackage = {
      def getTestCasePackageInner(node: Node, namespace: String = ""): TestCasePackage = {
        val name = node.attribute("name").get.head.text
        val currentNamespace = getAttribute(node, "type") match {
          case Some("Namespace") => if (namespace.isEmpty) name else s"$namespace.$name"
          case _ => namespace
        }

        val children = (node \ "results" \ "test-suite")
          .filter(n => getAttribute(n, "result").get != "Inconclusive")
          .map(n => getTestCasePackageInner(n, currentNamespace))
          .toList

        val testCases = (node \ "results" \ "test-case").map(tcNode => {
          val executed = getAttribute(tcNode, "executed").get.toBoolean
          val result = if (!executed) "Ignored" else if (getAttribute(tcNode, "success").get != "True") "Failure" else "Success"
          val (message, stackTrace) = if (result == "Failure")
            ((tcNode \\ "message").headOption.map(_.text), (tcNode \\ "stack-trace").headOption.map(_.text))
          else (None, None)
          val tcName: String = getAttribute(tcNode, "name").get

          val tcScreenshots = tcName match {
            case testNameRegex(className, methodName) =>
              screenshots.filter(s => s.url match {
                case screenshotQualifiedFileNameRegex(scrClassName, scrMethodName) => className == scrClassName && methodName == scrMethodName
                case screenshotFileNameRegex(scrMethodName) => methodName == scrMethodName
                case _ => false
              }).map(s => Artifact(s"$className.$methodName", s.url))
            case _ => Nil
          }

          TestCase(tcName, result, getAttribute(tcNode, "time").map(_.toDouble).getOrElse(0.0), message, tcScreenshots, stackTrace)
        }).toList

        TestCasePackage(if (currentNamespace.isEmpty) name else s"$namespace.$name", children, testCases)
      }

      getTestCasePackageInner(node)
    }

    testRunBuildNode.artifacts
      .find(a => a.name == "testResults")
      .flatMap(file => read(this.getArtifact(file.url)))
      .map(xmlString => (XML.loadString(xmlString) \ "test-suite").map(getTestCasePackage).toList)
      .getOrElse(Nil)
  }

  def getTestRun(branch: models.Branch, build: Int, part: String, run: String) = getBuild(branch, build)
    .map(b => b.getTestRunBuildNode(part, run))
    .flatten
    .map(testRunBuildNode => testRunBuildNode.copy(testResults = getTestCasePackages(testRunBuildNode)))

  private def getBuildsSources(branch: Branch): Traversable[BuildSource] = {
    val parsedBranchName = branch.name.replace('/', '_')
    val prRegex = "pr_(\\d+)_(\\d+)$".r
    val branchRegex = "(.+)_(\\d+)$".r
    val featureBranchRegex = "feature_(\\w+\\d*)$".r
    val pullRequestId = branch.pullRequest.map(_.prId)

    new File(directory)
      .listFiles()
      .reverse
      .view
      .filter(f => f.isDirectory)
      .map(f => f.getName match {
        case prRegex(prId, number) if pullRequestId.isDefined && pullRequestId.get == prId.toInt =>
          Some(f, branch, number.toInt, pullRequestId)
        case branchRegex(b, number) => parsedBranchName match {
          case featureBranchRegex(featureName) if featureName == b =>
            Some(f, branch, number.toInt, None)
          case branchName if branchName == b =>
            Some(f, branch, number.toInt, None)
          case branchName if b == parsedBranchName =>
            Some(f, branch, number.toInt, None)
          case branchName => None
        }
        case _ => None
      })
      .flatten
      .toList
      .sortBy(-_._3)
      .take(buildsPerBranch)
      .map(data => BuildSource(data._2, data._3, data._4, data._1))
  }

  private def getBuild(buildSource: BuildSource): Option[Build] = {
    val node = getBuildNode(new File(buildSource.file, "Build"))
    val folder = new File(buildSource.file, "Build/StartBuild")

    if (!folder.exists) {
      return None
    }
    val (status, _, timestamp) = getBuildDetails(folder)
    val commits = getCommits(new File(folder, "Checkout/GitChanges.log"))

    Some(Build(buildSource.number, buildSource.branch.name, status, new DateTime(timestamp), buildSource.pullRequestId.isDefined, commits = commits, node = node))
  }

  private def getCommits(file: File): List[Commit] = {
    if (!file.exists) {
      return Nil
    }

    val splitRegex = "(?m)^commit(?:(?:\r\n|[\r\n]).+$)*".r
    val commitRegex = "\\s*(\\w+)[\\r\\n].*[\\r\\n]?s*Author:\\s*(.*)\\s*<(.*)>[\\r\\n]\\s*Date:\\s+(.*)[\\r\\n]([\\w\\W]*)".r

    read(file) match {
      case Some(contents) => splitRegex.split(contents)
        .toList
        .filter(_.length > 0)
        .map {
          case commitRegex(sha1, name, email, date, comment) => Some(Commit(sha1, comment.trim, name, email, new DateTime(new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").parse(date).getTime)))
          case _ => None
        }
        .flatten
      case None => Nil
    }
  }

  private def getBuildNode(f: File): Option[BuildNode] = {
    val complexNameRegex = "(.+)_(.+)".r

    def getBuildNodeInner(folder: File, path: String): Option[BuildNode] = {
      if (!folder.exists) return None

      val (status, statusUrl, timestamp) = getBuildDetails(folder)

      val contents = folder.listFiles.sortBy(_.getName).toList
      val children: List[BuildNode] = contents
        .filter(f => f.isDirectory && !f.getName.startsWith("."))
        .flatMap(f => getBuildNodeInner(f, folder.getPath))
        .toList

      val artifacts = getArtifacts(contents)

      val (runName, name) = folder.getName match {
        case complexNameRegex(runNme, nme) => (runNme, nme)
        case nme => (nme, nme)
      }

      Some(BuildNode(name, runName, status, statusUrl.getOrElse(""), artifacts, new DateTime(timestamp), children))
    }

    //todo: add artifacts to root node
    val folder = new File(f, rootJobName)
    if (folder.exists) getBuildNodeInner(folder, f.getPath) else None
  }

  private def getBuildDetails(folder: File): (Option[String], Option[String], DateTime) = {
    val contents = folder.listFiles
    val (startedStatus, statusUrl, timestamp): (Option[String], Option[String], DateTime) = contents
      .find(_.getName.endsWith("started"))
      .map(file => {
      val (statusUrl, ts) = read(file).map(fc => {
        val rows = fc.split('\n')
        val statusUrl = rows(0)
        val ts = if (rows.length > 1)
          Some(new DateTime(new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(rows(1)).getTime))
        else
          None

        (Some(statusUrl), ts)
      })
        .getOrElse((None, None))

      (None, statusUrl, ts.getOrElse(new DateTime(file.lastModified)))
    })
      .getOrElse((Some("FAILURE"), None, new DateTime(folder.lastModified)))

    val status: Option[String] = startedStatus
      .orElse(contents.find(_.getName.endsWith("finished")).flatMap(read))
      .orElse(if ((DateTime.now - timeout) > timestamp) Some("TIMED OUT") else None)

    (status, statusUrl, timestamp)
  }
}