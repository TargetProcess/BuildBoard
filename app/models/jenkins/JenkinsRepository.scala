package models.jenkins

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.util.Try
import scalaj.http.{HttpOptions, Http}

object JenkinsRepository {
  private val jenkinsUrl = "http://jm2:8080"

  private val buildsQuery = "builds[number,url,actions[parameters[name,value]],subBuilds[buildNumber,jobName,result],number,result,timestamp]";
  private case class Build(number: Int, timestamp: DateTime, result: Option[String], url: String, actions: List[Action], subBuilds: List[SubBuild] = Nil)

  private case class Parameter(name: String, value: String)

  private case class Action(parameters: Option[List[Parameter]])

  private case class SubBuild(buildNumber: Int, jobName: String, result: Option[String])

  private case class DownstreamProject(name: String, url: String, builds: List[Build], downstreamProjects: Option[List[DownstreamProject]])

  private case class BuildInfo(builds: List[Build], downstreamProjects: List[DownstreamProject])

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

  private implicit val buildReads: Reads[Build] = (
    (__ \ "number").read[Int] ~
      (__ \ "timestamp").read[Long].map(new DateTime(_)) ~
      (__ \ "result").readNullable[String] ~
      (__ \ "url").read[String] ~
      (__ \ "actions").read(list[Action]) ~
      (__ \ "subBuilds").readNullable(list[SubBuild])
    )((number, timestamp, result, url, actions, subBuilds) => Build(number, timestamp, result, url, actions, if (subBuilds.isDefined) subBuilds.get else Nil))

  private implicit val downstreamProjectReads: Reads[DownstreamProject] = (
    (__ \ "name").read[String] ~
      (__ \ "url").read[String] ~
      (__ \ "builds").read(list[Build]) ~
      (__ \ "downstreamProjects").lazyReadNullable(list[DownstreamProject](downstreamProjectReads))
    )(DownstreamProject)

  private implicit val buildInfoReads: Reads[BuildInfo] = (
    (__ \ "builds").read(list[Build]) ~
      (__ \ "downstreamProjects").readNullable(list[DownstreamProject])
    )((builds: List[Build], projects: Option[List[DownstreamProject]]) => BuildInfo(builds, if (projects.isDefined) projects.get else Nil))

  private def getParameterValue(actions: List[Action], paramName: String) = actions.flatMap(a => a match {
    case Action(Some(parameters)) => {
      parameters.map(p => p match {
        case Parameter(name, value) if name == paramName => Some(value)
        case _ => None
      })
    }
    case _ => None
  })
  .flatten
  .headOption

  private def getBuildNodeFor(jobName: String, build: Build, downstreamProjects: List[DownstreamProject]): models.BuildNode = {
    val downstreamBuildNodes = for {subBuild <- build.subBuilds
                                    downstreamProject <- downstreamProjects if subBuild.jobName == downstreamProject.name
                                    downstreamBuild <- downstreamProject.builds if subBuild.buildNumber == downstreamBuild.number
    } yield getBuildNodeFor(subBuild.jobName, downstreamBuild, downstreamProject.downstreamProjects getOrElse Nil)

    models.BuildNode(build.number, jobName, build.result, build.url, getParameterValue(build.actions, "ARTIFACTS"), build.timestamp, downstreamBuildNodes)
  }

  private def getBuilds: List[models.Build] = Try {
    val rootJobName = "StartBuild"
    val url = s"$jenkinsUrl/job/$rootJobName/api/json?tree=$buildsQuery,downstreamProjects[name,url,$buildsQuery,downstreamProjects[name,url,$buildsQuery]]"
    val response = Http(url)
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))
      .asString
    val json = Json.parse(response)

    val buildInfo = buildInfoReads.reads(json).get

    buildInfo.builds.map(build => {
      val node = getBuildNodeFor(rootJobName, build, buildInfo.downstreamProjects)
      models.Build(node.number, getParameterValue(build.actions, "BRANCHNAME").get, node.status, node.statusUrl, node.timestamp, node)
    })
      .sortBy(-_.number)
  } getOrElse Nil

  def getBuilds(branch: String): List[models.Build] = {
    getBuilds.filter((b: models.Build) => b.branch == branch || b.branch == s"origin/$branch")
  }

  def getLastBuild(branch: String): Option[models.Build] = {
    getBuilds(branch).headOption
  }

  def getLastBuildsByBranch: Map[String, Option[models.Build]] = {
    getBuilds.groupBy(b => b.branch).map(item => (item._1, item._2.headOption))
  }

  def forceBuild(action: models.BuildAction with Product with Serializable) = {}
}
