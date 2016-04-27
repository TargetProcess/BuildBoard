package models.jenkins

import java.io.File
import java.text.SimpleDateFormat
import java.util.TimeZone

import com.github.nscala_time.time.Imports._
import models.{Artifact, Build, BuildNode, Commit, TestCase, TestCasePackage}
import org.joda.time.DateTime

import scala.xml.{Node, XML}
import src.Utils.watch

trait FileHelper {
  type Folder = File
}

trait ParseFolder extends Artifacts with FileHelper {

  protected val screenshotQualifiedFileNameRegex = """.*\\(\w+)\.(\w+)[-].*$""".r
  protected val screenshotFileNameRegex = """.*\\(\w+)[-].*$""".r
  protected val rootJobName = "StartBuild"

  private val timeout = 5.hours


  case class BuildSource(branch: String, number: Int, pullRequestId: Option[Int], folder: Folder, params: BuildParams)

  def getParamsFile(folder: Folder): File = {
    new File(folder, "Build/StartBuild/StartBuild.params")
  }


  def getBuild(buildSource: BuildSource, toggled: Boolean): Option[Build] = {
    val name: String = buildSource.folder.getName


    val node = watch(s"Get node ${buildSource.branch} ${buildSource.number}") {
      getBuildNode(new Folder(buildSource.folder, "Build"))
    }
    val folder = new Folder(buildSource.folder, "Build/StartBuild")


    if (folder.exists) {
      val commits = getCommits(new File(folder, "Checkout/GitChanges.log"))
      val ref = getRef(new File(folder, "Checkout/sha.txt"))

      getBuildDetails(folder)
        .map(buildDetails => Build(number = buildSource.number,
          branch = buildSource.branch,
          status = buildDetails.status,
          timestamp = buildDetails.startTime,
          timestampEnd  = buildDetails.endTime,
          toggled = toggled,
          commits = commits,
          ref = ref,
          pullRequestId = buildSource.pullRequestId,
          initiator = buildSource.params.parameters.get("WHO_STARTS"),
          description = buildSource.params.parameters.get("DESCRIPTION").orElse(buildSource.params.parameters.get("UID")),
          node = node,
          name = name,
          artifacts = getBuildArtifacts(folder)))
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
      FileApi.read(file).map(_.trim)
    }
  }

  val complexNameRegex = "(.+)_(.+)".r
  val mergerRegex = "(?s).*Merged by ([^(]*).*".r
  val buildNumberRegex = ".*/(\\d+)/".r

  private def getCommitName(name: String, comment: String): String = comment match {
    case mergerRegex(author) => s"$name [${author.trim}]"
    case _ => name
  }

  def unstableNodeNames: List[String]

  def isUnstable(name: String): Boolean = unstableNodeNames.contains(name)

  def getBuildNode(f: File): Option[BuildNode] = {

    def getBuildNodeInner(folder: File, path: String): Option[BuildNode] = {
      if (!folder.exists) return None

      val contents = folder.listFiles.sortBy(_.getName).toList
      val children: List[BuildNode] = contents
        .filter(f => f.isDirectory && !f.getName.startsWith("."))
        .flatMap(f => getBuildNodeInner(f, folder.getPath))

      val artifacts = getArtifacts(contents)

      val (runName, name) = folder.getName match {
        case complexNameRegex(runNme, nme) => (runNme, nme)
        case nme => (nme, nme)
      }

      val maybeBuildNode = getBuildDetails(folder)
        .map(buildDetails => BuildNode(
          buildDetails.number.toString,
          name,
          runName,
          buildDetails.number,
          buildDetails.status,
          buildDetails.statusUrl.getOrElse(""),
          if (buildDetails.statusUrl.isDefined) {
            Artifact("output", buildDetails.statusUrl.map(url => s"${url}consoleText").get) :: artifacts
          } else artifacts,
          buildDetails.startTime,
          Some(buildDetails.endTime.getOrElse(new DateTime(0))),
          buildDetails.rerun,
          children = children, isUnstable = Some(isUnstable(name))))

      //check if it's container job
      if (maybeBuildNode.isEmpty && children.length == 1) Some(children.head) else maybeBuildNode
    }

    //todo: add artifacts to root node
    val folder = new File(f, rootJobName)
    if (folder.exists) getBuildNodeInner(folder, f.getPath) else None
  }

  private case class BuildDetails(number: Int, status: Option[String], statusUrl: Option[String], startTime: DateTime, endTime:Option[DateTime], rerun: Option[Boolean])

  val dateFormat: SimpleDateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss Z")
  dateFormat.setTimeZone(TimeZone.getDefault)

  private def getBuildDetails(folder: File): Option[BuildDetails] = {
    val contents = folder.listFiles
    def getFile(extension: String): Option[File] = {
      contents.find(_.getName.endsWith(extension))
    }
    val startedFile: Option[File] = getFile("started")
    val finishedFile: Option[File] = getFile("finished")


    startedFile.map(started => {
      val startedFileContent: Option[Map[Int, String]] = FileApi.readAsMap(started)
      val statusUrl: Option[String] = startedFileContent.flatMap(_.get(0))

      val number = statusUrl.map { case buildNumberRegex(num) => num.toInt }.getOrElse(-1)

      val startTime = startedFileContent.flatMap(_.get(1))
        .map(x => new DateTime(dateFormat.parse(x+" +0300").getTime))
        .getOrElse(new DateTime(started.lastModified))

      val rerunRegex = "RERUN=(true|false)".r

      val rerun = startedFileContent.flatMap(_.get(2)).flatMap {
        case rerunRegex(value) => Some(value == "true")
        case _ => None
      }

      val status: Option[String] =
        finishedFile.flatMap(FileApi.read)
          .orElse(if ((DateTime.now - timeout) > startTime) Some("TIMED OUT") else None)

      val endTime = finishedFile.map(x=>new DateTime(x.lastModified))

      BuildDetails(number, status, statusUrl, startTime, endTime, rerun)
    })
  }

  def getTestCasePackages(testRunBuildNode: BuildNode): List[TestCasePackage] = {
    val screenshots = testRunBuildNode.artifacts.filter(a => a.name == screenshot)

    def getNUnitTestCasePackage(xml: String): List[TestCasePackage] = {
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

      (XML.loadString(xml) \ "test-suite").map(getTestCasePackage).toList
    }
    def getJUnitTestCasePackage(xml: String): List[TestCasePackage] = {
      def getTestCasePackage(node: Node): TestCasePackage = {
        def getTestCasePackageInner(node: Node, namespace: String = ""): TestCasePackage = {

          val name = node.attribute("name").map(a => a.head.text).getOrElse("All")
          val testCases = (node \ "testcase").map(tcNode => {
            val executed = (tcNode \ "skipped").length == 0
            val error = tcNode \ "error"
            val failure = tcNode \ "failure"
            val failureNode: Option[Node] = (if (error.length > 0) error else failure).headOption
            val result = if (!executed) "Ignored" else if (failureNode.isDefined) "Failure" else "Success"

            val (message, stackTrace) = failureNode.map(node => (getAttribute(node, "message"), Some(node.text))).getOrElse(None, None)
            val tcName: String = s"${getAttribute(tcNode, "classname").get}.${getAttribute(tcNode, "name").get}"

            TestCase(tcName, result, getAttribute(tcNode, "time").map(_.toDouble).getOrElse(0.0), message = message, stackTrace = stackTrace)
          }).toList

          TestCasePackage(name, Nil, testCases)
        }

        getTestCasePackageInner(node)
      }

      XML.loadString(xml).map(getTestCasePackage).toList
    }
    val nunitParser: PartialFunction[String, List[TestCasePackage]] = {
      case xml => getNUnitTestCasePackage(xml)
    }
    val junitParser: PartialFunction[String, List[TestCasePackage]] = {
      case xml => getJUnitTestCasePackage(xml)
    }

    val testResultAdapters = Map(
      ("nunit", nunitParser),
      ("junit", junitParser)
    )

    testRunBuildNode.artifacts
      .find(a => a.name == "testResults")
      .map(file => (file, FileApi.read(this.getArtifact(file.url))))
      .flatMap(fileXmlPair => {
        val fileName: String = new File(fileXmlPair._1.url).getName
        testResultAdapters
          .find(pair => fileName.toLowerCase.startsWith(pair._1))
          .flatMap(pair => fileXmlPair._2.map(xml => pair._2(xml)))
      })
      .getOrElse(Nil)
  }
}


