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

      if (needNotification(branch.name)) {


        val status = if (build.buildStatus == Toggled) "green" else build.buildStatus.name

        val icon: String = getIcon(build)

        val text = s"$icon *${build.branch}* is toggled to $status by *${loggedUser.fold("Unknown")(_.fullName)}*. <http://srv5>"

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

    def lastNotifiedBuild(branch: String): Option[Build] = buildMap.get(branch)

    def updateLastBuildInfo(branch: String, build: Build) = {
      buildMap(branch) = build

    }

    override def notifyAboutBuilds(allBuilds: List[Build]) = {
      for ((branch, builds) <- allBuilds.groupBy(x => x.name)) {

        if (needNotification(branch)) {
          if (!builds.isEmpty) {
            val lastBuild = builds.maxBy(_.number)
            val optionOldBuild: Option[Build] = lastNotifiedBuild(branch)


            optionOldBuild match {
              case Some(oldBuild) if oldBuild.number == lastBuild.number =>
                if (oldBuild.status != lastBuild.status) {
                  sendUpdateNotification(branch, lastBuild, optionOldBuild)
                }
              case _ => sendNewBuildNotification(branch, lastBuild, optionOldBuild)
            }

            updateLastBuildInfo(branch, lastBuild)
          }
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

    def needNotification(branch: String): Boolean = branch match {
      case BranchInfo.develop() => true
      case BranchInfo.hotfix(_) => true
      case BranchInfo.release(_) => true
      case _ => false
    }


    def sendNotification(prefix: String, branch: String, build: Build, oldBuild: Option[Build]) ={
      val status = build.buildStatus.obj
      val link = s"<http://srv5/#/list/branch?name=$branch|#${build.number}>"
      val oldText = oldBuild.fold("")(b => s"(was *${b.buildStatus.name}* at ${b.timestamp.toString("HH:mm dd/MM")})")
      val text = s"${getIcon(build)} $prefix $link on *$branch* now is *$status* at ${build.timestamp.toString("HH:mm dd/MM")} $oldText"
      post(text)

    }

    def sendUpdateNotification(branch: String, build: Build, oldBuild: Option[Build]) = {
      sendNotification("Build", branch, build, oldBuild)
    }

    def sendNewBuildNotification(branch: String, build: Build, oldBuild:Option[Build]) = {
      sendNotification("New build", branch, build, oldBuild)
    }


  }

  object NoNotifications extends NotificationService {

    override def notifyAboutBuilds(builds: List[Build]) = {}

    override def notifyToggle(branch: Branch, build: IBuildInfo) = {}
  }

}
