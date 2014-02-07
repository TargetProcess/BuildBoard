package models.jenkins

import scala.util.Try
import scalaj.http.Http
import play.api.Play
import models._
import java.io.{Serializable, File}
import scala.io.Source
import play.api.Play.current
import scala.xml.{Node, XML}
import models.BuildNode
import scala.Some
import models.TestCase
import models.Build
import models.TestCasePackage
import org.joda.time.DateTime

object JenkinsAdapter extends BuildsRepository with JenkinsApi {
  val screenshotQualifiedFileNameRegex = """.*\\(\w+)\.(\w+)[-].*$""".r
  val screenshotFileNameRegex = """.*\\(\w+)[-].*$""".r

  val complexNameRegex = "(.+)_(.+)".r

  val testNameRegex = """.*\.(\w+)\.(\w+)$""".r


  private val SCREENSHOT: String = "screenshot"
  private val directory = Play.configuration.getString("jenkins.data.path").get

  override def getBuilds: List[Build] = new File(directory)
    .listFiles
    .filter(_.isDirectory)
    .map(getBuild)
    .toList

  private def getBuild(f: File): Build = {
    val prRegex = "pr_(\\d+)_(\\d+)".r
    val branchRegex = "(\\w+)_(\\d+)".r
    val (number, branch) = f.getName match {
      case prRegex(prId, n) => (n.toInt, s"pr/$prId")
      case branchRegex(br, n) => (n.toInt, br)
    }
    val node = getBuildNode(new File(f, "Build"))

    Build(number, branch, node.status, node.statusUrl, node.timestamp, node)
  }

  private def getBuildNode(f: File): BuildNode = {
    def getBuildNodeInner(folder: File, path: String): BuildNode = {
      val contents = folder.listFiles.sortBy(_.getName).toList
      val (startedStatus, statusUrl, timestamp) = contents.filter(file => file.getName.endsWith("started")) match {
        case file :: Nil =>
          val (statusUrl, ts) = read(file)
            .map(fc => {
            val rows = fc.split('\n')
            val statusUrl = rows(0)
            val ts = if (rows.length > 1) Some(new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(rows(1)).getTime) else None
            (Some(statusUrl), ts)
          })
            .getOrElse((None, Some(file.lastModified)))
          (None, statusUrl, ts.getOrElse(file.lastModified))
        case Nil => (Some("FAILURE"), None, folder.lastModified)
      }
      val status: Option[String] = startedStatus.orElse(contents.find(_.getName.endsWith("finished")).flatMap(read))



      val children = contents
        .filter(f => f.isDirectory && !f.getName.startsWith("."))
        .map(f => getBuildNodeInner(f, folder.getPath)).toList

      val artifacts = getArtifacts(contents)

      //todo: associate screenshots with tests

      folder.getName match {
        case complexNameRegex(runName, name) => BuildNode(name, runName, status, statusUrl.getOrElse(""), artifacts, new DateTime(timestamp), children)
        case name => BuildNode(name, name, status, statusUrl.getOrElse(""), artifacts, new DateTime(timestamp), children)
      }
    }

    //todo: add artifacts to root node
    getBuildNodeInner(new File(f, rootJobName), f.getPath)
  }

  private def getArtifacts(contents: List[File]): List[Artifact] = {
    def getArtifactsInner(file: File, filter: File => Boolean, artifactName: String): List[Artifact] = file.listFiles
      .filter(filter)
      .map(_.getPath.substring(directory.length + 1))
      .map(Artifact(artifactName, _))
      .toList

    contents.map(file => file.getName match {
      case ".TestResults" => getArtifactsInner(file, f => f.getName.endsWith(".xml"), "testResults")
      case ".Logs" => getArtifactsInner(file, f => f.getName.startsWith("SessionLogs"), "logs")
      case ".Screenshots" => getArtifactsInner(file, _ => true, SCREENSHOT)
      case _ => List()
    })
      .flatten
  }

  private def read(f: File): Option[String] = Try {
    Source.fromFile(f).mkString
  }.toOption

  def getTestCasePackages(testRunBuildNode: BuildNode): List[TestCasePackage] = {
    val screenshots = testRunBuildNode.artifacts.filter(a => a.name == SCREENSHOT)

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

  def getArtifact(file: String): File = new File(directory, file)

  private def getAttribute(n: Node, key: String): Option[String] = n.attribute(key).map(_.headOption.map(_.text)).flatten
}

trait JenkinsApi {
  private val jenkinsUrl = Play.configuration.getString("jenkins.url").get
  val rootJobName = "StartBuild"

  def forceBuild(action: models.BuildAction) = Try {
    Http.post(s"$jenkinsUrl/job/$rootJobName/buildWithParameters")
      .params(action.parameters)
      .asString
  }
}
