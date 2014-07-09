package models.jenkins

import java.io.File
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._
import models._
import scala.xml.{Node, XML}
import models.Artifact
import models.Commit
import models.BuildNode
import scala.Some
import models.TestCase
import models.Build
import models.TestCasePackage


trait ParseFolder extends FileApi with Artifacts {

  protected val screenshotQualifiedFileNameRegex = """.*\\(\w+)\.(\w+)[-].*$""".r
  protected val screenshotFileNameRegex = """.*\\(\w+)[-].*$""".r

  protected val rootJobName = "StartBuild"

  type Folder = File
  private val timeout = 5.hours

  case class BuildSource(branch: String, number: Int, pullRequestId: Option[Int], folder: Folder, params: BuildParams)

  def getBuild(buildSource: BuildSource, toggled: Boolean): Option[Build] = {
    val name: String = buildSource.folder.getName

    play.Logger.info(s"getBuild ${buildSource.branch} - $name")

    val node = getBuildNode(new Folder(buildSource.folder, "Build"))
    val folder = new Folder(buildSource.folder, "Build/StartBuild")

    if (!folder.exists) {
      return None
    }
    val (status, _, timestamp) = getBuildDetails(folder)
    val commits = getCommits(new File(folder, "Checkout/GitChanges.log"))
    val ref = getRef(new File(folder, "Checkout/sha.txt"))

    Some(
      Build(number = buildSource.number,
        branch = buildSource.branch,
        status = status,
        timestamp = new DateTime(timestamp),
        toggled = toggled,
        commits = commits,
        ref = ref,
        pullRequestId = buildSource.pullRequestId,
        initiator = buildSource.params.parameters.get("WHO_STARTS"),
        node = node,
        name = name)
    )
  }

  val splitRegex = "(?m)^commit(?:(?:\r\n|[\r\n]).+$)*".r
  val commitRegex = """\s*(\w+)[\r\n].*[\r\n]?s*Author:\s*(.*)\s*<(.*)>[\r\n]\s*Date:\s+(.*)[\r\n]([\w\W]*)""".r

  def getCommits(file: File): List[Commit] = {
    if (!file.exists) {
      return Nil
    }

    read(file) match {
      case Some(contents) => splitRegex.split(contents)
        .toList
        .filter(_.length > 0)
        .map {
        case commitRegex(sha1, name, email, date, comment) =>
          val normalizedComment = comment.trim
          val commitName = getCommitName(name, normalizedComment)
          Some(Commit(sha1, normalizedComment, commitName, email, new DateTime(new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").parse(date).getTime)))
        case _ => None
      }
        .flatten
      case None => Nil
    }
  }

  def getRef(file: File) = {
    if (!file.exists) {
      None
    }  else{
      read(file)
    }
  }

  private def getCommitName(name: String, comment: String) = {
    val mergerRegex = "(?s).*Merged by ([^(]*).*".r
    comment match {
      case mergerRegex(author) => s"$name [${author.trim}]"
      case _ => name
    }
  }

  def getBuildNode(f: File): Option[BuildNode] = {
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

  def getBuildDetails(folder: File): (Option[String], Option[String], DateTime) = {
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

          TestCase(tcName, result, getAttribute(tcNode, "time").fold(0.0)(_.toDouble), message, tcScreenshots, stackTrace)
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

}


