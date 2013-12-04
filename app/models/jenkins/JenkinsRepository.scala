package models.jenkins

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.util.{Try, Success, Failure}
import scalaj.http.{HttpException, HttpOptions, Http}
import com.github.nscala_time.time.Imports._
import play.api.libs.json.JsString
import scala.Some

object JenkinsRepository {

  def getBuilds(branch: String): List[models.Build] = getBuilds.filter((b: models.Build) => b.branch == branch || b.branch == s"origin/$branch")

  def getLastBuild(branch: String): Option[models.Build] = getBuilds(branch).headOption

  def getLastBuildsByBranch: Map[String, Option[models.Build]] = getBuilds.groupBy(b => b.branch).map(item => (item._1, item._2.headOption))

  def getBuild(branch: String, number: Int): Option[models.Build] = {
    //get build info
    //get build nodes with active configuration nodes
    //if build node has active configuration then fetch builds from corresponding downstream job


        getBuildInfo match{
          case Success(buildInfo) => {
            val res = getRunsJobs(rootJobName, rootJobJsonUrl, buildInfo.downstreamProjects, Nil).map(runJob => {
               getRunsInfo(runJob._2) match {
                 case Success(runs) => {
                   println(runs)
                   None
                 }
                 case Failure(e) => {
                   println(e)
                   None
                 }
               }
            })
            println(res)
          }
          case Failure(e) => {
            println(e)
            None
          }
        }

//        getBuilds(branch).filter(b => b.number == number).headOption
    None
  }

  def forceBuild(action: models.BuildAction with Product with Serializable) = {}

  private val jenkinsUrl = "http://jm2:8080"
  private val rootJobName = "StartBuild"

  private def jobUrl(jobName: String) = s"$jenkinsUrl/job/$jobName"

  private def toJsonUrl(url: String) = s"$url/api/json"

  private val rootJobJsonUrl = toJsonUrl(jobUrl(rootJobName))
  private val activeConfigurationsQuery = "activeConfigurations[name,url]"
  private val buildsQuery = s"builds[number,url,actions[parameters[name,value]],subBuilds[buildNumber,jobName,result],number,result,timestamp]"
  private val runsQuery = s"builds[runs[actions[parameters[name,value]],timestamp,result,number,url]]"
  private val buildsAndActiveConfigurationQuery = s"$buildsQuery,$activeConfigurationsQuery"

  private case class Parameter(name: String, value: String)

  private case class Action(parameters: Option[List[Parameter]])

  private case class BuildNode(number: Int, name: String, status: Option[String], statusUrl: String, artifactsUrl: Option[String], timestamp: DateTime, children: List[BuildNode] = Nil)

  private case class SubBuild(buildNumber: Int, jobName: String, result: Option[String])

  private case class Build(number: Int, timestamp: DateTime, result: Option[String], url: String, actions: List[Action], subBuilds: List[SubBuild] = Nil)

  private case class ActiveConfiguration(name: String, url: String)

  private case class DownstreamProject(name: String, url: String, builds: List[Build], downstreamProjects: List[DownstreamProject], activeConfigurations: List[ActiveConfiguration])

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
    )((number, timestamp, result, url, actions, subBuilds) => Build(number, timestamp, result, url, actions, subBuilds.getOrElse(Nil)))

  private implicit val activeConfigurationReads: Reads[ActiveConfiguration] = Json.reads[ActiveConfiguration]

  private implicit val downstreamProjectReads: Reads[DownstreamProject] = (
    (__ \ "name").read[String] ~
      (__ \ "url").read[String] ~
      (__ \ "builds").read(list[Build]) ~
      (__ \ "downstreamProjects").lazyReadNullable(list[DownstreamProject](downstreamProjectReads)) ~
      (__ \ "activeConfigurations").readNullable(list[ActiveConfiguration])
    )((name, url, builds, downstreamProjects, activeConfigurations) => DownstreamProject(name, url, builds, downstreamProjects.getOrElse(Nil), activeConfigurations.getOrElse(Nil)))

  private implicit val buildInfoReads: Reads[BuildInfo] = (
    (__ \ "builds").read(list[Build]) ~
      (__ \ "downstreamProjects").readNullable(list[DownstreamProject])
    )((builds: List[Build], projects: Option[List[DownstreamProject]]) => BuildInfo(builds, projects.getOrElse(Nil)))


  private def getBuildInfo: Try[BuildInfo] = Try {
    val url = s"$rootJobJsonUrl?tree=$buildsAndActiveConfigurationQuery,downstreamProjects[name,url,$buildsAndActiveConfigurationQuery,downstreamProjects[name,url,$buildsAndActiveConfigurationQuery]]"
    val response = Http(url)
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))
      .asString
    val json = Json.parse(response)

    buildInfoReads.reads(json).get
  }

    private def getRunsInfo(url: String) = Try {
      val url = s"$rootJobJsonUrl?tree=$runsQuery"
      val response = Http(url)
        .option(HttpOptions.connTimeout(1000))
        .option(HttpOptions.readTimeout(5000))
        .asString
      val json = Json.parse(response)

      json.validate((__ \\ "runs").read(list[Build])).get
    }

  private def getParameterValue(actions: List[Action], paramName: String) = actions.flatMap {
    case Action(Some(parameters)) =>
      parameters.map {
        case Parameter(name, value) if name == paramName => Some(value)
        case _ => None
    }
    case _ => None
  }
  .flatten
  .headOption

  private def makeBuildNode(jobName: String, build: Build, downstreamProjects: List[DownstreamProject]): models.BuildNode = {
    val downstreamBuildNodes = for {subBuild <- build.subBuilds
                                    downstreamProject <- downstreamProjects if subBuild.jobName == downstreamProject.name
                                    downstreamBuild <- downstreamProject.builds if subBuild.buildNumber == downstreamBuild.number
    } yield makeBuildNode(subBuild.jobName, downstreamBuild, downstreamProject.downstreamProjects)

    models.BuildNode(build.number, jobName, build.result, build.url, getParameterValue(build.actions, "ARTIFACTS"), build.timestamp, downstreamBuildNodes)
  }

  private def getRunsJobs(jobName: String, url: String, downstreamProjects: List[DownstreamProject], activeConfigurations: List[ActiveConfiguration]): Set[(String, String)] = {
    val childrenRuns = downstreamProjects.flatMap(p => getRunsJobs(p.name, p.url, p.downstreamProjects, p.activeConfigurations)).toSet
    activeConfigurations match {
      case Nil => childrenRuns
      case _ => childrenRuns + Tuple2(jobName, url)
    }
  }

  private def makeBuild(build: Build, buildInfo: BuildInfo) = {
    val node = makeBuildNode(rootJobName, build, buildInfo.downstreamProjects)

      models.Build(node.number, getParameterValue(build.actions, "BRANCHNAME").get, node.status, node.statusUrl, node.timestamp, node)
  }

  private def makeBuilds(buildInfo: BuildInfo) = buildInfo.builds.map(build => {
    makeBuild(build, buildInfo)
    })
      .sortBy(-_.number)

  private def getBuilds: List[models.Build] = getBuildInfo match {
    case Success(buildInfo) => makeBuilds(buildInfo)
    case Failure(_) => List()
  }
}
