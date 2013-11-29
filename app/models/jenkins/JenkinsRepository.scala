package models.jenkins

import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import models.{BuildNode, Build}
import org.joda.time.DateTime
import scala.util.Try
import scalaj.http.{HttpOptions, Http}
import play.api.libs.json.Json.JsValueWrapper


object JenkinsRepository {
  val jenkinsUrl = "http://jm2:8080"

  case class Parameter(name: String, value: String)

  case class Action(parameters: Option[List[Parameter]])

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

  implicit val buildReads: Reads[Build] = (
    (__ \ "timestamp").read[Long].map(new DateTime(_)) ~
      (__ \ "result").read[String] ~
      (__ \ "url").read[String] ~
      (__ \ "actions").read(list[Action])
        .map((actions: List[Action]) => actions.map(a => a match {
            case Action(Some(parameters)) => {
              parameters.map(p => p match {
                case Parameter("BRANCHNAME", value) => Some(value)
                case _ => None
              })
                .filter(b => b.nonEmpty)
                .map(_.get)
                .headOption
            }
            case _ => None
          })
        .flatten
        .filter(b => b.nonEmpty)
        .headOption
      )
    )((timestamp, result, url, branchName) => Build(branchName.get, result, url, timestamp, BuildNode("1", "1", "1")))

  def getBuilds(branch: String): List[Build] = Try {
    val url = s"$jenkinsUrl/job/StartBuild/api/json?depth=2&tree=builds[url,actions[parameters[name,value]],number,result,timestamp]"
    val response = Http(url)
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))
      .asString
    val json = Json.parse(response)

    json.validate((__ \ "builds").read(list[Build])).get
      .filter((b: Build) => b.branch == branch || b.branch == s"origin/$branch")
  } getOrElse Nil

  def getLastBuild(branch: String): Option[Build] = getBuilds(branch).headOption
}
