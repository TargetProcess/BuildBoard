package models.notifications

import components.{LoggedUserProviderComponent, NotificationComponent}
import models.{BranchInfo, BuildInfo}
import play.api.Play
import play.api.Play.current
import scalaj.http.{HttpOptions, Http}


trait NotificationComponentImpl extends NotificationComponent {

  this: NotificationComponentImpl with LoggedUserProviderComponent =>

  val notificationService: NotificationService =
    (for (slackUrl <- Play.configuration.getString("slack.url");
          slackChannel <- Play.configuration.getString("slack.channel")
    ) yield new NotificationServiceImpl(slackUrl, slackChannel)) getOrElse NoNotifications

  class NotificationServiceImpl(slackUrl: String, slackChannel: String)
    extends NotificationService {

    override def notifyToggle(build: BuildInfo): Unit = {

      val needNotify = build.branch match {
        case BranchInfo.develop() => true
        case BranchInfo.hotfix(_) => true
        case BranchInfo.release(_) => true
        case _ => false
      }

      if (needNotify) {
        val status = if (build.toggled) "green" else build.status.getOrElse("unknown").toLowerCase
        val icon = if (build.toggled || status.equalsIgnoreCase("success")) ":white_check_mark:" else ":x:"
        Http
          .postData(slackUrl, s"""{
             |"channel": "$slackChannel",
             |"text": "$icon *${build.branch}* is toggled to $status by *${loggedUser.map(_.fullName).getOrElse("Unknown")}*. <http://srv5>"
            }""".stripMargin)
          .option(HttpOptions.connTimeout(1000))
          .option(HttpOptions.readTimeout(5000))
          .asString

      }
    }
  }

  object NoNotifications extends NotificationService {
    override def notifyToggle(build: BuildInfo): Unit = {}
  }

}
