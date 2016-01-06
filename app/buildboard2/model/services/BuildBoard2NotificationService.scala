package buildboard2.model.services

import buildboard2.Writes2._
import buildboard2.components.DefaultRegistry
import buildboard2.model.{Account, BuildInfo}
import models.notifications.BuildNotification
import play.api.Play
import play.api.Play.current
import play.api.libs.json.Json

import scalaj.http.{Http, HttpOptions}

object BuildBoard2NotificationService {
  val registry = new DefaultRegistry
  val url = Play.configuration.getString("buildboard2.url").get

  def start() = BuildNotification.subject.subscribe(build =>
    for (account <- registry.accountRepository.getAll) {
      notifyBuild(account, new BuildInfo(build))
    }
  )

  def notifyBuild(account: Account, buildInfo: BuildInfo): Unit = {
    try {
      Http.postData(s"$url/api/builds/${account.toolToken}", Json.stringify(Json.toJson(buildInfo)))
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
