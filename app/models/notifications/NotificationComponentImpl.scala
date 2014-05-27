package models.notifications

import components.{UserRepositoryComponent, LoggedUserProviderComponent, NotificationComponent}
import models._
import play.api.Play
import play.api.Play.current
import scalaj.http.{HttpException, HttpOptions, Http}
import models.BuildStatus.Toggled
import com.mongodb.casbah.Imports._
import models.Branch
import scala.Some
import scala.collection.immutable.Iterable


trait NotificationComponentImpl extends NotificationComponent {

  this: NotificationComponentImpl with LoggedUserProviderComponent with UserRepositoryComponent =>

  val notificationService: NotificationService =
    (for (slackUrl <- Play.configuration.getString("slack.url");
          slackChannel <- Play.configuration.getString("slack.channel")
    ) yield new NotificationServiceImpl(slackUrl, slackChannel)) getOrElse NoNotifications

  class NotificationServiceImpl(slackUrl: String, broadcastChannel: String) extends NotificationService {

    val baseUrl = Play.configuration.getString("base.url").get

    override def notifyToggle(branch: Branch, build: IBuildInfo): Unit = {

      if (needBroadcast(branch.name)) {


        val status = if (build.buildStatus == Toggled) "green" else build.buildStatus.name

        val icon: String = getIcon(build)

        val text = s"$icon *${build.branch}* is toggled to $status by *${loggedUser.fold("Unknown")(_.fullName)}*. <$baseUrl>"

        post(text, broadcastChannel)

      }
    }


    val buildMap = scala.collection.mutable.Map[String, IBuildInfo]()


    override def notifyAboutBuilds(updatedBuilds: List[IBuildInfo]) = {

      val lastBuilds: Iterable[IBuildInfo] = updatedBuilds.groupBy(_.branch)
        .map {
        case (_, builds) => builds.maxBy(_.number)
      }


      for (build <- lastBuilds) {

        val prevBuild = buildMap.get(build.branch)

        prevBuild match {
          case Some(oldBuild) =>
            if (oldBuild.status != build.status) {
              sendNotification(build, prevBuild)
            }
          case None =>
            sendNotification(build, None)
        }

        buildMap(build.branch) = build

      }
    }

    def needBroadcast(branch: String): Boolean = branch match {
      case BranchInfo.develop() => true
      case BranchInfo.hotfix(_) => true
      case BranchInfo.release(_) => true
      case _ => false
    }

    def sendNotification(currentBuild: IBuildInfo, optionLastBuild: Option[IBuildInfo]) {
      val was = optionLastBuild.fold("")(lastBuild => s"(was *${lastBuild.buildStatus.name}* at ${lastBuild.timestamp.toString("HH:mm dd/MM")})")

      val branch = currentBuild.branch
      val status = currentBuild.buildStatus.obj

      val link = s"<$baseUrl/#/list/branch?name=$branch|#${currentBuild.number}>"
      val now = s"Build $link on *$branch* now is *$status* at ${currentBuild.timestamp.toString("HH:mm dd/MM")}"

      val text = s"${getIcon(currentBuild)} $now $was"


      if (needBroadcast(currentBuild.branch)) {
        post(text, broadcastChannel)
      }

      currentBuild.initiator
        .flatMap(userRepository.findOneByFullName)
        .flatMap(_.slackName)
        .foreach(slackName => post(text, "@" + slackName))

    }


    def post(text: String, channel: String) {
      val data = s"""{"channel": "$channel","text": "$text"}"""
      play.Logger.info(data)
      try {
        Http
          .postData(slackUrl, data)
          .option(HttpOptions.connTimeout(1000))
          .option(HttpOptions.readTimeout(5000))
          .asString
      }
      catch {
        case e: HttpException => play.Logger.error(e.body, e)
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

  }

  object NoNotifications extends NotificationService {

    override def notifyAboutBuilds(builds: List[IBuildInfo]) = {}

    override def notifyToggle(branch: Branch, build: IBuildInfo) = {}
  }

}
