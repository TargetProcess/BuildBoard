package models.notifications

import components.{LoggedUserProviderComponent, NotificationComponent}
import models._
import play.api.Play
import play.api.Play.current
import scalaj.http.{HttpOptions, Http}
import models.BuildInfo
import models.BuildStatus.Toggled
import com.novus.salat.dao.ModelCompanion
import com.mongodb.casbah.Imports._
import models.Branch
import scala.Some
import models.Build
import se.radley.plugin.salat.Binders.ObjectId


trait NotificationComponentImpl extends NotificationComponent {

  this: NotificationComponentImpl with LoggedUserProviderComponent =>

  val notificationService: NotificationService =
    (for (slackUrl <- Play.configuration.getString("slack.url");
          slackChannel <- Play.configuration.getString("slack.channel")
    ) yield new NotificationServiceImpl(slackUrl, slackChannel)) getOrElse NoNotifications

  class NotificationServiceImpl(slackUrl: String, slackChannel: String)
    extends NotificationService {

    override def notifyToggle(branch: Branch, build: IBuildInfo): Unit = {

      if (needNotification(branch)) {


        val status = if (build.buildStatus == Toggled) "green" else build.buildStatus.name

        val icon: String = getIcon(build)

        val text = s"$icon *${build.branch}* is toggled to $status by *${loggedUser.map(_.fullName).getOrElse("Unknown")}*. <http://srv5>"

        post(text)

      }
    }


    def getIcon(build: IBuildInfo): String = {
      val icon = build.buildStatus.success match {
        case Some(true) => ":white_check_mark:"
        case Some(false) => ":x:"
        case None => ":running:"
      }
      icon
    }

    val buildMap = scala.collection.mutable.Map[String, Build]()

    def lastNotifiedBuild(branch: Branch): Option[Build] = buildMap.get(branch.name)

    def updateLastBuildInfo(branch: Branch, build: Build) = {
      buildMap(branch.name) = build

    }

    override def notifyAboutBuilds(branch: Branch, builds: List[Build]) = {
      if (needNotification(branch)) {
        if (!builds.isEmpty) {
          val lastBuild = builds.maxBy(_.number)
          val optionOldBuild: Option[Build] = lastNotifiedBuild(branch)

          
          if (optionOldBuild.isDefined){
            val oldBuild = optionOldBuild.get
            
            if (oldBuild.number == lastBuild.number){
              if (oldBuild.status != lastBuild.status) {
                sendUpdateNotification(branch, lastBuild, oldBuild.buildStatus)
              }
            }
            else {
              sendNewBuildNotification(branch, lastBuild, optionOldBuild)
            }
          }
          else {
            sendNewBuildNotification(branch, lastBuild, None)
          }
          updateLastBuildInfo(branch, lastBuild)
        }
      }
    }


    def post(text: String) {
      val data = s"""{"channel": "$slackChannel","text": "$text"}"""

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


    def sendUpdateNotification(branch: Branch, build: Build, oldStatus: BuildStatus) = {
      val status = build.buildStatus.obj
      val text = s"${getIcon(build)} Build <http://srv5/#/list/branch?name=${branch.name}|#${build.number}> on *${branch.name}* now is *$status* at ${build.timestamp.toString("HH:mm dd/MM")} (was ${oldStatus.name})"
      post(text)
    }

    def sendNewBuildNotification(branch: Branch, build: Build, oldBuild:Option[Build]) = {
      val status = build.buildStatus.obj
      val oldText = oldBuild.map(b=>s" (was ${b.buildStatus.name} at ${b.timestamp.toString("HH:mm dd/MM")})").getOrElse("")
      val text = s"${getIcon(build)} New build <http://srv5/#/list/branch?name=${branch.name}|#${build.number}> on *${branch.name}* is *$status* at ${build.timestamp.toString("HH:mm dd/MM")}$oldText"
      post(text)
    }


  }

  object NoNotifications extends NotificationService {

    override def notifyAboutBuilds(branch: Branch, builds: List[Build]) = {}

    override def notifyToggle(branch: Branch, build: IBuildInfo) = {}
  }

}
