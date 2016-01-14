package buildboard2.model.services

import buildboard2.Writes2._
import buildboard2.components.DefaultRegistry
import buildboard2.model.{Account, Artifact2, Build2, Job2}
import models.notifications.BuildNotification
import models.{Artifact, Build, BuildNode}
import play.api.Play
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}
import scalaj.http.{Http, HttpOptions}

object BuildBoard2CacheService {
  val registry = new DefaultRegistry
  val buildBoard2Url = Play.configuration.getString("buildboard2.url").get
  val baseUrl = Play.configuration.getString("base.url").get

  def start() = BuildNotification.subject
    .map(build => processBuild(build))
    .subscribe(onNext = {
      case Success(b) => play.Logger.info(s"Build ${b.number} processed and imported to BuildBoard2")
      case Failure(e) => play.Logger.error(e.getMessage)
    })

  def processBuild(build: Build): Try[Build] = Try {
    val build2 = Build2.create(build)

    def getJobs(node: BuildNode, parentNode: Option[BuildNode] = None): List[Job2] = {
      Job2.create(build, node, parentNode) :: node.children.flatMap(c => getJobs(c, Some(node)))
    }

    def getArtifactUrl(a: Artifact): String = if (a.name != "output") s"$baseUrl/buildboard2/artifact?file=${a.url}" else a.url


    def getArtifacts(node: BuildNode): List[Artifact2] = {
      node.artifacts.map(a => Artifact2(a.url, a.name, getArtifactUrl(a), Some(Job2.getId(build, node)))) ::: node.children.flatMap(getArtifacts)
    }

    val jobs = build.node
      .map(getJobs(_))
      .getOrElse(Nil)

    val artifacts = build.artifacts
      .map(a => Artifact2(a.url, a.name, getArtifactUrl(a), build = Some(Build2.getId(build)))) ::: build.node
      .map(getArtifacts)
      .getOrElse(Nil)

    for (job <- jobs) {
      registry.job2Repository.save(job)
    }

    for (artifact <- artifacts) {
      registry.artifact2Repository.save(artifact)
    }

    for (account <- registry.accountRepository.getAll) {
      notifyBuild(account, build2)
      notifyJobs(account, jobs)
      artifacts.grouped(200).foreach(artifacts2 => notifyArtifacts(account, artifacts2))
    }

    build
  }

  def notifyBuild(account: Account, build2: Build2) = notify(account, "builds", Json.toJson(build2))

  def notifyJobs(account: Account, jobs2: List[Job2]) = notify(account, "jobs", Json.toJson(Map("items" -> jobs2)))

  def notifyArtifacts(account: Account, artifacts2: List[Artifact2]) = notify(account, "artifacts", Json.toJson(Map("items" -> artifacts2)))

  def notify(account: Account, resource: String, item: JsValue) = {
    try {
      Http.postData(s"$buildBoard2Url/api/$resource/${account.toolToken}", Json.stringify(item))
        .header("content-type", "application/json")
        .option(HttpOptions.connTimeout(1000))
        .option(HttpOptions.readTimeout(5000))
        .asString
    }
    catch {
      case e: Throwable =>
        play.Logger.error(e.getMessage)
    }
  }
}
