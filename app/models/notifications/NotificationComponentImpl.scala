package models.notifications

import components.NotificationComponent
import models.{BranchInfo, BuildInfo, Build, AuthInfo}
import play.api.Play
import play.api.Play.current
import src.Utils.watch
import components.DefaultComponent
import scalaj.http.Http


trait NotificationComponentImpl extends NotificationComponent {
  val notificationService: NotificationService =
    (for (slackUrl <- Play.configuration.getString("slack.url");
         slackChannel <-Play.configuration.getString("slack.channel")
    ) yield new NotificationServiceImpl(slackUrl, slackChannel)) getOrElse NoNotifications



  class NotificationServiceImpl(slackUrl:String, slackChannel:String)
    extends NotificationService {

    override def notifyToggle(build: BuildInfo): Unit = {

      val status = if (build.toggled) "Toggled" else build.status.getOrElse("Unknown")


      build.branch match {
        case BranchInfo.develop => Http
          .post(slackUrl)
          .param("payload",
            s"""
              |"channel": "#$slackChannel",
              |"text": "Branch ${build.branch}" is toggled to $status by
            """.stripMargin)

      }

    }
  }

  object NoNotifications extends    NotificationService{
    override def notifyToggle(build: BuildInfo): Unit = {}
  }

}
