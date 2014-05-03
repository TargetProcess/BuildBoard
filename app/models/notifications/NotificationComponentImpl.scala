package models.notifications

import components.{LoggedUserProviderComponent, NotificationComponent}
import models._
import play.api.Play
import play.api.Play.current
import scalaj.http.{HttpOptions, Http}
import models.BuildStatus.Toggled
import com.mongodb.casbah.Imports._
import models.Branch
import scala.Some


trait NotificationComponentImpl extends NotificationComponent {

  this: NotificationComponentImpl with LoggedUserProviderComponent =>

  val notificationService: NotificationService =
    (for (slackUrl <- Play.configuration.getString("slack.url");
          slackChannel <- Play.configuration.getString("slack.channel")
    ) yield new NotificationServiceImpl(slackUrl, slackChannel)) getOrElse NoNotifications

  class NotificationServiceImpl(slackUrl: String, broadcastChannel: String)
    extends NotificationService {

    override def notifyToggle(branch: Branch, build: IBuildInfo): Unit = {

      if (needBroadcast(branch.name)) {


        val status = if (build.buildStatus == Toggled) "green" else build.buildStatus.name

        val icon: String = getIcon(build)

        val text = s"$icon *${build.branch}* is toggled to $status by *${loggedUser.fold("Unknown")(_.fullName)}*. <http://srv5>"

        post(text, broadcastChannel)

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

    val buildMap = scala.collection.mutable.Map[String, IBuildInfo]()

    def lastNotifiedBuild(branch: String): Option[IBuildInfo] = buildMap.get(branch)

    def updateLastBuildInfo(branch: String, build: IBuildInfo) = {
      buildMap(branch) = build

    }

    override def notifyAboutBuilds(allBuilds: List[IBuildInfo]) = {
      for ((branch, builds) <- allBuilds.groupBy(x => x.branch)) {

        if (!builds.isEmpty) {
          val lastBuild = builds.maxBy(_.number)
          val optionOldBuild: Option[IBuildInfo] = lastNotifiedBuild(branch)


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


    def post(text: String, channel:String) {
      val data = s"""{"channel": "$channel","text": "$text"}"""

      Http
        .postData(slackUrl, data)
        .option(HttpOptions.connTimeout(1000))
        .option(HttpOptions.readTimeout(5000))
        .asString
    }

    def needBroadcast(branch: String): Boolean = branch match {
      case BranchInfo.develop() => true
      case BranchInfo.hotfix(_) => true
      case BranchInfo.release(_) => true
      case _ => false
    }


    def sendNotification(prefix: String, branch: String, build: IBuildInfo, oldBuild: Option[IBuildInfo], channel:String) = {
      val status = build.buildStatus.obj
      val link = s"<http://srv5/#/list/branch?name=$branch|#${build.number}>"
      val oldText = oldBuild.fold("")(b => s"(was *${b.buildStatus.name}* at ${b.timestamp.toString("HH:mm dd/MM")})")
      val text = s"${getIcon(build)} $prefix $link on *$branch* now is *$status* at ${build.timestamp.toString("HH:mm dd/MM")} $oldText"
      post(text, channel)

    }

    def sendUpdateNotification(branch: String, build: IBuildInfo, oldBuild: Option[IBuildInfo]) = {
      send("Build", branch, build, oldBuild)
    }

    def sendNewBuildNotification(branch: String, build: IBuildInfo, oldBuild: Option[IBuildInfo]) = {
      send("New build", branch, build, oldBuild)
    }


    def send(prefix: String, branch: String, build: IBuildInfo, oldBuild: Option[IBuildInfo]) {
      if (needBroadcast(branch)) {
        sendNotification(prefix, branch, build, oldBuild, broadcastChannel)
      }




    }



  }

  object NoNotifications extends NotificationService {

    override def notifyAboutBuilds(builds: List[IBuildInfo]) = {}

    override def notifyToggle(branch: Branch, build: IBuildInfo) = {}
  }

}
