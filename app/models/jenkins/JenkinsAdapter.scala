package models.jenkins

import scala.util.Try
import scalaj.http.Http
import scala.Some
import play.api.Play
import models.{BuildNode, Build}
import java.io.File
import com.github.nscala_time.time.StaticForwarderImports.DateTime
import com.github.nscala_time.time.TypeImports.DateTime
import scala.io.Source
import play.api.Play.current

object JenkinsAdapter extends BuildsRepository with JenkinsApi  {
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
    val node = getBuildNode(new File(f, s"Build/$rootJobName"))

    Build(number, branch, node.status, node.statusUrl, DateTime.now, node)
  }

  private def getBuildNode(f: File): BuildNode = {
    val complexNameRegex = "(.+)_(.+)".r
    val (runName, name) = f.getName match {
      case complexNameRegex(runName, name) => (runName, name)
      case runName => (runName, runName)
    }
    val contents = f.listFiles.sortBy(_.getName).toList
    val (startedStatus, statusUrl, timestamp) = contents.filter(f => f.getName.endsWith("started")) match {
      case file::Nil => (None, read(file), file.lastModified)
      case Nil => (Some("FAILURE"), None, f.lastModified)
    }
    val status = if (startedStatus.isDefined) startedStatus else
      contents.filter(f => f.getName.endsWith("finished")) match {
        case file::Nil => read(file)
        case Nil => startedStatus
      }
    val children = contents
      .filter(f => f.isDirectory && !f.getName.startsWith("."))
      .map(f => getBuildNode(f)).toList

    BuildNode(name, runName, status, statusUrl.getOrElse(""), None, new DateTime(timestamp), children)
  }

  private def read(f: File): Option[String] = Try{ Some(Source.fromFile(f).mkString) }.getOrElse(None)
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
