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

  def getBuild(branch: String, number: Int): Option[models.Build] = getBuilds(branch).filter(b => b.number == number).headOption

  def forceBuild(action: models.BuildAction) = Try {
    Http.post(s"$jenkinsUrl/job/$rootJobName/buildWithParameters")
      .params(action.parameters)
      .asString
  }

  private def makeBuildNode(jobName: String, build: Build, downstreamProjects: List[DownstreamProject], runs: List[models.BuildNode] = Nil): models.BuildNode = {
    val downstreamBuildNodes = (for {subBuild <- build.subBuilds
                                     downstreamProject <- downstreamProjects if subBuild.jobName == downstreamProject.name
                                     downstreamBuild <- downstreamProject.builds if subBuild.buildNumber == downstreamBuild.number
    } yield makeBuildNode(subBuild.jobName, downstreamBuild, downstreamProject.downstreamProjects, runs))

    models.BuildNode(build.number, jobName, build.result, build.url, getParameterValue(build.actions, "ARTIFACTS"), build.timestamp, downstreamBuildNodes ++ runs.filter(r => r.name == jobName && r.number == build.number))
  }

  private def makeRunsBuildNodes(jobName: String, build: Build, downstreamProjects: List[DownstreamProject], buildRuns: JobBuildRuns): List[models.BuildNode] = {
    (for {subBuild <- build.subBuilds
          downstreamProject <- downstreamProjects if subBuild.jobName == downstreamProject.name
          downstreamBuild <- downstreamProject.builds if subBuild.buildNumber == downstreamBuild.number
    } yield makeRunsBuildNodes(subBuild.jobName, downstreamBuild, downstreamProject.downstreamProjects, buildRuns) :: downstreamProject.activeConfigurations match {
        case Nil => Nil
        case _ => for {buildRun <- buildRuns.builds
                       run <- buildRun.runs
        } yield models.BuildNode(run.number, buildRuns.name, run.result, run.url, getParameterValue(run.actions, "ARTIFACTS"), run.timestamp)
      })
      .flatMap(nodes => nodes)
  }

  private def getRuns(buildInfo: BuildInfo): List[models.BuildNode] = getRunsJobs(rootJobName, rootJobJsonUrl, buildInfo.downstreamProjects, Nil).map(runJob => {
    getBuildRuns(runJob._2) match {
      case Success(buildRuns) => buildInfo.builds.map(build => makeRunsBuildNodes(runJob._1, build, buildInfo.downstreamProjects, buildRuns))
      case Failure(e) => Nil
    }
  })
    .flatten
    .flatten
    .toList

  private def getRunsJobs(jobName: String, url: String, downstreamProjects: List[DownstreamProject], activeConfigurations: List[ActiveConfiguration]): Set[(String, String)] = {
    val childrenRuns = downstreamProjects.flatMap(p => getRunsJobs(p.name, p.url, p.downstreamProjects, p.activeConfigurations)).toSet

    activeConfigurations match {
      case Nil => childrenRuns
      case _ => childrenRuns + Tuple2(jobName, url)
    }
  }

  private def makeBuild(build: Build, buildInfo: BuildInfo, runs: List[models.BuildNode] = Nil) = {
    val node = makeBuildNode(rootJobName, build, buildInfo.downstreamProjects, runs)

    models.Build(node.number, getParameterValue(build.actions, "BRANCHNAME").get, node.status, node.statusUrl, node.timestamp, node)
  }

  private def makeBuilds(buildInfo: BuildInfo, runs: List[models.BuildNode] = Nil) = buildInfo.builds.map(build => {
    makeBuild(build, buildInfo, runs)
  })
    .sortBy(-_.number)

  private def getBuilds: List[models.Build] = getBuildInfo match {
    case Success(buildInfo) => makeBuilds(buildInfo, getRuns(buildInfo))
    case Failure(_) => Nil
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

  private val jenkinsUrl = "http://jm2:8080"
  private val rootJobName = "StartBuild"

  private def jobUrl(jobName: String) = s"$jenkinsUrl/job/$jobName/"

  private def toJsonUrl(url: String) = s"${url}api/json"

  private val rootJobJsonUrl = toJsonUrl(jobUrl(rootJobName))
  private val activeConfigurationsQuery = "activeConfigurations[name,url]"
  private val buildsQuery = s"builds[number,url,actions[parameters[name,value]],subBuilds[buildNumber,jobName,result],result,timestamp]"
  private val runsQuery = s"name,builds[runs[actions[parameters[name,value]],timestamp,result,number,url]]"
  private val buildsAndActiveConfigurationQuery = s"$buildsQuery,$activeConfigurationsQuery"

  private case class Parameter(name: String, value: String)

  private case class Action(parameters: Option[List[Parameter]])

  private case class BuildNode(number: Int, name: String, status: Option[String], statusUrl: String, artifactsUrl: Option[String], timestamp: DateTime, children: List[BuildNode] = Nil)

  private case class SubBuild(buildNumber: Int, jobName: String, result: Option[String])

  private case class Build(number: Int, timestamp: DateTime, result: Option[String], url: String, actions: List[Action], subBuilds: List[SubBuild] = Nil)

  private case class Run(number: Int, timestamp: DateTime, result: Option[String], url: String, actions: List[Action])

  private case class JobBuildRuns(name: String, builds: List[BuildRuns])

  private case class BuildRuns(runs: List[Run])

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
    )((number, timestamp, result, url, actions, subBuilds) => Build(number, timestamp, result, url, actions.filter((a: Action) => a.parameters.isDefined), subBuilds.getOrElse(Nil)))

  private implicit val runReads: Reads[Run] = (
    (__ \ "number").read[Int] ~
      (__ \ "timestamp").read[Long].map(new DateTime(_)) ~
      (__ \ "result").readNullable[String] ~
      (__ \ "url").read[String] ~
      (__ \ "actions").read(list[Action])
    )((number, timestamp, result, url, actions) => Run(number, timestamp, result, url, actions.filter((a: Action) => a.parameters.isDefined)))

  private implicit val buildRuns: Reads[BuildRuns] = Json.reads[BuildRuns]

  private implicit val jobBuildRunsReads: Reads[JobBuildRuns] = Json.reads[JobBuildRuns]

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

  private def getBuildRuns(jobUrl: String): Try[JobBuildRuns] = Try {
    val url = s"${toJsonUrl(jobUrl)}?tree=$runsQuery"
    val response = Http(url)
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))
      .asString
    val json = Json.parse(response)

    jobBuildRunsReads.reads(json).get
  }
}
