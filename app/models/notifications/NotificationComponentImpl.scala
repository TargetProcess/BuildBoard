package models.notifications

import components.{LoggedUserProviderComponent, NotificationComponent}
import models.{Build, Branch, BranchInfo, BuildInfo}
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

    override def notifyToggle(branch: Branch, build: BuildInfo): Unit = {

      if (needNotification(branch)) {
        val status = if (build.toggled) "green" else build.status.getOrElse("unknown").toLowerCase
        val icon = if (build.toggled || status.equalsIgnoreCase("success")) ":white_check_mark:" else ":x:"

        val text = s"$icon *${build.branch}* is toggled to $status by *${loggedUser.map(_.fullName).getOrElse("Unknown")}*. <http://srv5>"

        post(text)

      }
    }

    def post(text: String) {
      val data = s"""{
             |"channel": "$slackChannel",
             |"text": "$text"
            }""".stripMargin

      Http
        .postData(slackUrl, data)
        .option(HttpOptions.connTimeout(1000))
        .option(HttpOptions.readTimeout(5000))
        .asString
    }

    def needNotification(branch: Branch): Boolean = branch.name match {
      case BranchInfo.develop() => true
      case BranchInfo.hotfix(_) => true
      case BranchInfo.release(_) => true
      case _ => false
    }

    def lastNotifiedBuild(branch: Branch): Option[Build] = None

    def sendUpdateNotification(branch: Branch, build: Build) = {}

    def sendNewBuildNotification(branch: Branch, build: Build) = {

      val process = build.status.isDefined
      val text = s"New build on *${branch.name}* "

    }

    override def notifyBuilds(branch: Branch, builds: List[Build]) = {
      if (needNotification(branch)) {
        val lastBuild = builds.maxBy(_.number)
        val optionBuild:Option[Build] = lastNotifiedBuild(branch).filter(_.number == lastBuild.number)

        optionBuild match {
          case Some(build) if build.status != lastBuild.status => sendUpdateNotification(branch, lastBuild)
          case _ => sendNewBuildNotification(branch, lastBuild)
        }



      }
    }
  }

  object NoNotifications extends NotificationService {

    override def notifyBuilds(branch: Branch, builds: List[Build]) = {}

    override def notifyToggle(branch: Branch, build: BuildInfo) = {}
  }

}
