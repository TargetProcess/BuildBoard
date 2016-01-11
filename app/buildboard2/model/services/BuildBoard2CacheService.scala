package buildboard2.model.services

import buildboard2.Writes2._
import buildboard2.components.DefaultRegistry
import buildboard2.model.{Job2, Account, Build2}
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
    .subscribe(onNext = build => build match {
      case Success(b) => play.Logger.info(s"Build ${b.number} processed and imported to BuildBoard2")
      case Failure(e) => play.Logger.error(e.getMessage)
    })

  def processBuild(build: Build): Try[Build] = Try {
    val build2 = Build2.create(build)

    updateBuild(build2)

    def getJobs(node: BuildNode, parentNode: Option[BuildNode] = None): List[Job2] = {
      Job2.create(build, node, parentNode) :: node.children.flatMap(c => getJobs(c, Some(node)))
    }

    val jobs = build.node
      .map(getJobs(_))
      .getOrElse(Nil)

    for (job <- jobs) {
      updateJob(job)
    }

    for (account <- registry.accountRepository.getAll) {
      notifyBuild(account, build2)
      for (job <- jobs) {
        notifyJob(account, job)
      }
    }

    build
  }

  def updateBuild(build2: Build2) = {
    registry.build2Repository.remove(build2.id)
    registry.build2Repository.save(build2)
  }

  def updateJob(job2: Job2) = {
    registry.job2Repository.remove(job2.id)
    registry.job2Repository.save(job2)
  }

  def notifyBuild(account: Account, build2: Build2) = notify(account, "builds", Json.toJson(build2))

  def notifyJob(account: Account, job2: Job2) = notify(account, "jobs", Json.toJson(job2))

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
