package models.jenkins

import java.io.File
import java.text.SimpleDateFormat

import com.github.nscala_time.time.Imports._
import models.{Artifact, Build, BuildNode, Commit, TestCase, TestCasePackage}
import org.joda.time.DateTime

import scala.xml.{Node, XML}


trait ParseFolder extends Artifacts {
  type Folder = File

  protected val screenshotQualifiedFileNameRegex = """.*\\(\w+)\.(\w+)[-].*$""".r
  protected val screenshotFileNameRegex = """.*\\(\w+)[-].*$""".r
  protected val rootJobName = "StartBuild"

  private val timeout = 5.hours

  val unstableNodeNames:List[String]

  case class BuildSource(branch: String, number: Int, pullRequestId: Option[Int], folder: Folder, params: BuildParams)

  def getBuild(buildSource: BuildSource, toggled: Boolean): Option[Build] = {
    val name: String = buildSource.folder.getName

    play.Logger.info(s"getBuild ${buildSource.branch} - $name")

    val node = getBuildNode(new Folder(buildSource.folder, "Build"))
    val folder = new Folder(buildSource.folder, "Build/StartBuild")


    if (folder.exists) {
      val buildDetails = getBuildDetails(folder)
      val commits = getCommits(new File(folder, "Checkout/GitChanges.log"))
      val ref = getRef(new File(folder, "Checkout/sha.txt"))

      Some(
        Build(number = buildSource.number,
          branch = buildSource.branch,
          status = buildDetails.status,
          timestamp = buildDetails.timestamp,
          toggled = toggled,
          commits = commits,
          ref = ref,
          pullRequestId = buildSource.pullRequestId,
          initiator = buildSource.params.parameters.get("WHO_STARTS"),
          node = node,
          name = name)
      )
    } else {
      None
    }
  }

  val splitRegex = "(?m)^commit(?:(?:\r\n|[\r\n]).+$)*".r
  val commitRegex = """\s*(\w+)[\r\n].*[\r\n]?s*Author:\s*(.*)\s*<(.*)>[\r\n]\s*Date:\s+(.*)[\r\n]([\w\W]*)""".r

  def getCommits(file: File): List[Commit] = {
    if (!file.exists) {
      return Nil
    }

    FileApi.read(file) match {
      case Some(contents) => splitRegex.split(contents)
        .toList
        .filter(_.length > 0).flatMap {
        case commitRegex(sha1, name, email, date, comment) =>
          val normalizedComment = comment.trim
          val commitName = getCommitName(name, normalizedComment)
          Some(Commit(sha1, normalizedComment, commitName, email, new DateTime(new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").parse(date).getTime)))
        case _ => None
      }
      case None => Nil
    }
  }

  def getRef(file: File) = {
    if (!file.exists) {
      None
    } else {
      FileApi.read(file)
    }
  }

  val complexNameRegex = "(.+)_(.+)".r
  val mergerRegex = "(?s).*Merged by ([^(]*).*".r

  private def getCommitName(name: String, comment: String): String = comment match {
    case mergerRegex(author) => s"$name [${author.trim}]"
    case _ => name
  }


  def isUnstable(name: String): Boolean = unstableNodeNames.contains(name)

  def getBuildNode(f: File): Option[BuildNode] = {

    def getBuildNodeInner(folder: File, path: String): Option[BuildNode] = {
      if (!folder.exists) return None

      val buildDetails = getBuildDetails(folder)

      val contents = folder.listFiles.sortBy(_.getName).toList
      val children: List[BuildNode] = contents
        .filter(f => f.isDirectory && !f.getName.startsWith("."))
        .flatMap(f => getBuildNodeInner(f, folder.getPath))

      val artifacts = getArtifacts(contents)

      val (runName, name) = folder.getName match {
        case complexNameRegex(runNme, nme) => (runNme, nme)
        case nme => (nme, nme)
      }

      Some(BuildNode(
        name,
        runName,
        buildDetails.status,
        buildDetails.statusUrl.getOrElse(""),
        artifacts,
        buildDetails.timestamp,
        buildDetails.rerun,
        children = children, isUnstable = Some(isUnstable(name))))
    }

    //todo: add artifacts to root node
    val folder = new File(f, rootJobName)
    if (folder.exists) getBuildNodeInner(folder, f.getPath) else None
  }

  private case class BuildDetails(status: Option[String], statusUrl: Option[String], timestamp: DateTime, rerun: Option[Boolean])

  val dateFormat: SimpleDateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss")

  private def getBuildDetails(folder: File): BuildDetails = {
    val contents = folder.listFiles
    val startedFile: Option[File] = contents.find(_.getName.endsWith("started"))

    val startedFileContent: Option[Map[Int, String]] = startedFile.flatMap(FileApi.readAsMap)


    val statusUrl = startedFileContent.flatMap(_.get(0))
    val timestamp = startedFileContent.flatMap(_.get(1))
      .map(x => new DateTime(dateFormat.parse(x).getTime))
      .orElse(startedFile.map(x => new DateTime(x.lastModified)))
      .getOrElse(new DateTime(folder.lastModified))

    val rerunRegex = "RERUN=(true|false)".r

    val rerun = startedFileContent.flatMap(_.get(2)).flatMap{
      case rerunRegex(value) => Some(value == "true")
      case _ => None
    }

    val startedStatus = if (startedFile.isDefined) None else Some("FAILURE")

    val status: Option[String] = startedStatus
      .orElse(contents.find(_.getName.endsWith("finished")).flatMap(FileApi.read))
      .orElse(if ((DateTime.now - timeout) > timestamp) Some("TIMED OUT") else None)

    BuildDetails(status, statusUrl, timestamp, rerun)
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
      .flatMap(file => FileApi.read(this.getArtifact(file.url)))
      .map(xmlString => (XML.loadString(xmlString) \ "test-suite").map(getTestCasePackage).toList)
      .getOrElse(Nil)
  }

}


