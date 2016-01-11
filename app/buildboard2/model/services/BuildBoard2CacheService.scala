package buildboard2.model.services

import buildboard2.Writes2._
import buildboard2.components.DefaultRegistry
import buildboard2.model.{Artifact2, Job2, Account, Build2}
import models.{BuildNode, Build}
import models.notifications.BuildNotification
import play.api.Play
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import scala.util.{Try, Failure, Success}
import scalaj.http.{Http, HttpOptions}

object BuildBoard2CacheService {
  val registry = new DefaultRegistry
  val url = Play.configuration.getString("buildboard2.url").get

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

    def getArtifacts(node: BuildNode): List[Artifact2] = {
      node.artifacts.map(a => Artifact2(a.url, a.name, s"${registry.config.jenkinsDataPath}/${a.url}", Job2.getId(node))) ::: node.children.flatMap(getArtifacts)
    }

    val jobs = build.node
      .map(getJobs(_))
      .getOrElse(Nil)

    val artifacts = build.node
      .map(getArtifacts)
      .getOrElse(Nil)

    for (job <- jobs) {
      registry.job2Repository.save(job)
    }

    for (account <- registry.accountRepository.getAll) {
      notifyBuild(account, build2)

      for (job <- jobs) {
        notifyJob(account, job)
      }

      for (artifact <- artifacts) {
        notifyArtifact(account, artifact)
      }
    }

    build
  }

  def notifyBuild(account: Account, build2: Build2) = notify(account, "builds", Json.toJson(build2))

  def notifyJob(account: Account, job2: Job2) = notify(account, "jobs", Json.toJson(job2))

  def notifyArtifact(account: Account, artifact2: Artifact2) = notify(account, "artifacts", Json.toJson(artifact2))

  def notify(account: Account, resource: String, item: JsValue) = {
    try {
      Http.postData(s"$url/api/$resource/${account.toolToken}", Json.stringify(item))
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
