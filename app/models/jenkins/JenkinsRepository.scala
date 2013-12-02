package models.jenkins

import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import models.{BuildAction, BuildNode, Build}
import org.joda.time.DateTime
import scala.util.Try
import scalaj.http.{HttpOptions, Http}
import play.api.libs.json.Json.JsValueWrapper


object JenkinsRepository {
  def forceBuild(action: BuildAction with Product with Serializable)={}

  val jenkinsUrl = "http://jm2:8080"

  case class Parameter(name: String, value: String)

  case class Action(parameters: Option[List[Parameter]])

  case class SubBuild(buildNumber: Int, jobName: String, result: Option[String])

  case class SubBuilds(subBuilds: Option[List[SubBuild]])

  private implicit val parameterReads: Reads[Parameter] = (
      (__ \ "name").read[String] ~
      (__ \ "value").read[JsValue]
    )((name, value) => {
    value match {
      case JsString(str) => Parameter(name, str)
      case _ => Parameter(name, value.toString)
    }
  })
  private implicit val actionReads: Reads[Action] = Json.reads[Action]

  private implicit val subBuildReads: Reads[SubBuild] = Json.reads[SubBuild]

  implicit val buildReads: Reads[Build] = (
    (__ \ "number").read[Int] ~
      (__ \ "timestamp").read[Long].map(new DateTime(_)) ~
      (__ \ "result").readNullable[String] ~
      (__ \ "url").read[String] ~
      (__ \ "actions").read(list[Action])
        .map((actions: List[Action]) => actions.map {
        case Action(Some(parameters)) => parameters.map {
          case Parameter("BRANCHNAME", value) => Some(value)
          case _ => None
        }
          .filter(b => b.nonEmpty)
          .map(_.get)
          .headOption
        case _ => None
      }
        .flatten.find(b => b.nonEmpty)
      ) ~
      (__ \ "subBuilds").read(list[SubBuild])
    )((number, timestamp, result, url, branchName, subBuilds) => {
    val buildNodes = Some(subBuilds.map((s: SubBuild) => BuildNode(s.buildNumber, s.jobName, s.result, "no", "no", None)))
    Build(number, branchName.get, result, url, timestamp, BuildNode(number, "StartBuild", result, url, "1", buildNodes))
  })

  def getBuilds: List[Build] = Try {
    val url = s"$jenkinsUrl/job/StartBuild/api/json?depth=2&tree=builds[number,url,actions[parameters[name,value]],subBuilds[buildNumber,jobName,result],number,result,timestamp]"
    val response = Http(url)
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))
      .asString
    val json = Json.parse(response)

    json.validate((__ \ "builds").read(list[Build])).get
  } getOrElse Nil

  def getBuilds(branch: String): List[Build] = getBuilds.filter((b: Build) => b.branch == branch || b.branch == s"origin/$branch")

  def getLastBuild(branch: String): Option[Build] = {
    getBuilds(branch).headOption
  }

  def getLastBuildsByBranch: List[Build] = {
    getBuilds
  }
}
