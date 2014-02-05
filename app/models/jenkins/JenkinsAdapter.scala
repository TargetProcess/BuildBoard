package models.jenkins

import scala.util.Try
import scalaj.http.Http
import play.api.Play
import models._
import java.io.File
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
  private val directory = Play.configuration.getString("jenkins.data.path").get

  override def getBuilds: List[Build] = new File(directory)
    .listFiles
    .filter(_.isDirectory)
    .map(getBuild _)
    .toList

  private def getBuild(f: File): Build = {
    val prRegex = "pr_(\\d+)_(\\d+)".r
    val branchRegex = "(\\w+)_(\\d+)".r
    val (number, branch) = f.getName match {
      case prRegex(prId, number) => (number.toInt, s"pr/$prId")
      case branchRegex(branch, number) => (number.toInt, branch)
    }
    val node = getBuildNode(new File(f, "Build"))

    Build(number, branch, node.status, node.statusUrl, node.timestamp, node)
  }

  private def getBuildNode(f: File): BuildNode = {
    def getBuildNodeInner(file: File, path: String): BuildNode = {
      val complexNameRegex = "(.+)_(.+)".r
      val (runName, name) = file.getName match {
        case complexNameRegex(runName, name) => (runName, name)
        case runName => (runName, runName)
      }
      val contents = file.listFiles.sortBy(_.getName).toList
      val (startedStatus, statusUrl, timestamp) = contents.filter(f => f.getName.endsWith("started")) match {
        case file :: Nil =>
          val (statusUrl, ts) = read(file)
          .map(fc => {
            val rows = fc.split('\n')
            val statusUrl = rows(0)
            val ts = if (rows.length > 1) Some(new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm:ss").parse(rows(1)).getTime) else None
            (Some(statusUrl), ts)
          })
          .getOrElse((None, Some(file.lastModified)))
          (None, statusUrl, ts.getOrElse(file.lastModified))
        case Nil => (Some("FAILURE"), None, file.lastModified)
      }
      val status = if (startedStatus.isDefined) startedStatus
      else contents.filter(f => f.getName.endsWith("finished")) match {
        case file :: Nil => read(file)
        case Nil => startedStatus
      }
      val children = contents
        .filter(f => f.isDirectory && !f.getName.startsWith("."))
        .map(f => getBuildNodeInner(f, file.getPath)).toList

      val artifacts = getArtifacts(contents)

      BuildNode(name, runName, status, statusUrl.getOrElse(""), artifacts, new DateTime(timestamp), children)
    }

    //todo: add artifacts to root node
    getBuildNodeInner(new File(f, rootJobName), f.getPath)
  }

  private def getArtifacts(contents: List[File]): List[Artifact] = {
    val testResultsUrl = (contents.filter(f => f.getName == ".TestResults").headOption match {
      case Some(folder) => folder.listFiles
        .filter(_.getName.endsWith(".xml"))
        .headOption
        .map(_.getPath.substring(directory.length + 1))
      case None => None
    }).map(url => List(Artifact("testResults", url)))
    .getOrElse(List())

    val logs = (contents.filter(f => f.getName == ".Logs").headOption match {
      case Some(folder) => folder.listFiles
        .filter(_.getName.startsWith("SessionLogs"))
        .headOption
        .map(_.getPath.substring(directory.length + 1))
      case None => None
    }).map(url => List(Artifact("logs", url)))
    .getOrElse(List())

    testResultsUrl:::logs
  }

  private def read(f: File): Option[String] = Try {
    Some(Source.fromFile(f).mkString)
  }.getOrElse(None)

  def getTestCasePackages(file: String): List[TestCasePackage] = {
    read(new File(directory, file)) match {
      case None => List()
      case Some(xmlString) =>
        val xml = XML.loadString(xmlString)
        (xml \ "test-suite").map(getTestCasePackage _).toList
    }
  }

  private def getTestCasePackage(node: Node): TestCasePackage = {
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
        val (message, stackTrace) = if (result == "Failure") ((tcNode \\ "message").headOption.map(_.text), (tcNode \\ "stack-trace").headOption.map(_.text)) else (None, None)

        TestCase(getAttribute(tcNode, "name").get, result, getAttribute(tcNode, "time").getOrElse("0").toDouble, message, stackTrace)
      }).toList

      TestCasePackage(if (currentNamespace.isEmpty) name else s"$namespace.$name", children, testCases)
    }

    getTestCasePackageInner(node)
  }

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
