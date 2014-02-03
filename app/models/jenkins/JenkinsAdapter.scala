package models.jenkins

import scala.util.Try
import scalaj.http.Http
import scala.Some
import play.api.Play
import models.{TestCase, TestCasePackage, BuildNode, Build}
import java.io.File
import com.github.nscala_time.time.StaticForwarderImports.DateTime
import com.github.nscala_time.time.TypeImports.DateTime
import scala.io.Source
import play.api.Play.current
import scala.xml.{Node, XML, Elem}

object JenkinsAdapter extends BuildsRepository with JenkinsApi {
  private val directory = Play.configuration.getString("jenkins.data.path").get

  override def getBuilds: List[Build] = new File(directory)
    .listFiles
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

    Build(number, branch, node.status, node.statusUrl, DateTime.now, node)
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
        case file :: Nil => (None, read(file), file.lastModified)
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

      val artifactsUrl = contents.filter(f => f.getName == ".TestResults").headOption match {
        case Some(folder) => folder.listFiles
          .filter(_.getName.endsWith(".xml"))
          .headOption
          .map(_.getPath.substring(directory.length + 1))
        case None => None
      }

      BuildNode(name, runName, status, statusUrl.getOrElse(""), artifactsUrl, new DateTime(timestamp), children)
    }

    getBuildNodeInner(new File(f, rootJobName), f.getPath)
  }

  private def read(f: File): Option[String] = Try {
    Some(Source.fromFile(f).mkString)
  }.getOrElse(None)

  def getArtifact(file: String): List[TestCasePackage] = {
    read(new File(directory, file)) match {
      case None => List()
      case Some(xmlString) =>
        val xml = XML.loadString(xmlString)
        (xml \ "test-suite").map(getTestCasePackage _).toList
    }
  }

  private def getTestCasePackage(node: Node): TestCasePackage = {
    val name = node.attribute("name").get.head.text
    val children = (node \ "results" \ "test-suite")
      .filter(n => getAttribute(n, "result").get != "Inconclusive")
      .map(getTestCasePackage _)
      .toList
    val testCases = (node \ "results" \ "test-case").map(tcNode => {
      val result = getAttribute(tcNode, "result").get
      val (message, stackTrace) = if (result != "Success") ((tcNode \\ "message").headOption.map(_.text), (tcNode \\ "stack-trace").headOption.map(_.text)) else (None, None)

      TestCase(getAttribute(tcNode, "name").get, getAttribute(tcNode, "executed").get.toBoolean, result, getAttribute(tcNode, "duration").getOrElse("0").toDouble, message, stackTrace)
    }).toList

    TestCasePackage(name, children, testCases)
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
