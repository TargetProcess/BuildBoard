package models.notifications

import components.{GithubServiceComponent, UserRepositoryComponent, LoggedUserProviderComponent, NotificationComponent}
import models._
import play.api.Play
import play.api.Play.current
import scalaj.http.{HttpException, HttpOptions, Http}
import models.BuildStatus._
import com.mongodb.casbah.Imports._
import models.Branch
import scala.Some
import scala.collection.immutable.Iterable
import models.github.GithubStatus


trait NotificationComponentImpl extends NotificationComponent {

  this: NotificationComponentImpl
    with LoggedUserProviderComponent
    with UserRepositoryComponent
    with GithubServiceComponent
  =>

  val notificationService: NotificationService = {
    (for (
      config <- Play.configuration.getConfig("slack");
      slackUrl <- config.getString("url");
      slackChannel <- config.getString("broadcastChannel");
      sendPrivateNotifications <- Some(config.getBoolean("sendPrivateNotifications").getOrElse(true))
    ) yield new NotificationServiceImpl(slackUrl, slackChannel, sendPrivateNotifications)
      ) getOrElse NoNotifications
  }

  class NotificationServiceImpl(slackUrl: String, broadcastChannel: String, sendPrivateNotifications: Boolean) extends NotificationService {

    val baseUrl = Play.configuration.getString("base.url").get

    def updateGithub(build: Build) = {
      build.ref.foreach(sha => {
        val statusString = build.buildStatus match {
          case Unknown | InProgress => "pending"
          case Toggled | Ok => "success"
          case Failure => "error"
          case Aborted | TimedOut => "failure"
        }

        val text = s"Build is ${build.buildStatus.obj} at ${build.timestamp.toString("HH:mm dd/MM")}"

        if (!build.isPullRequest) {
          play.Logger.info(s"Set status for $sha => $statusString")
          githubService.setStatus(sha, GithubStatus(statusString, getBuildLink(build), text, "continuous-integration/jenkins"))
        }
      })
    }

    override def notifyToggle(branch: Branch, build: Build): Unit = {

      if (needBroadcast(branch.name)) {

        val status = if (build.buildStatus == Toggled) "green" else build.buildStatus.name

        val icon: String = getIcon(build)

        val text = s"$icon *${build.branch}* is toggled to $status by *${loggedUser.fold("Unknown")(_.fullName)}*. <${getBuildLink(build)}|#${build.number}>"

        post(text, broadcastChannel)

      }
      updateGithub(build)

    }


    val buildMap = scala.collection.mutable.Map[String, Build]()


    override def notifyAboutBuilds(updatedBuilds: List[Build]) = {

      val lastBuilds: Iterable[Build] = updatedBuilds.groupBy(_.branch)
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

    def sendNotification(currentBuild: Build, optionLastBuild: Option[Build]) {
      val was = optionLastBuild.fold("")(lastBuild => s"(was *${lastBuild.buildStatus.name}* at ${lastBuild.timestamp.toString("HH:mm dd/MM")})")

      val link = getBuildLink(currentBuild)

      val branch = currentBuild.branch
      val status = currentBuild.buildStatus.obj

      val now = s"Build <$link|#${currentBuild.number}> on *$branch* now is *$status* at ${currentBuild.timestamp.toString("HH:mm dd/MM")}"

      val text = s"${getIcon(currentBuild)} $now $was"


      if (needBroadcast(currentBuild.branch)) {
        post(text, broadcastChannel)
      }

      updateGithub(currentBuild)


      if (sendPrivateNotifications) {
        currentBuild.initiator
          .flatMap(userRepository.findOneByFullName)
          .flatMap(_.slackName)
          .foreach(slackName => post(text, "@" + slackName))
      }

    }


    def getBuildLink(currentBuild: Build): String = s"$baseUrl/#/list/branch?name=${currentBuild.branch}"

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


    def getIcon(build: Build): String = {
      val icon = build.buildStatus.success match {
        case Some(true) => ":white_check_mark:"
        case Some(false) => ":x:"
        case None => ":running:"
      }
      icon
    }

  }

  object NoNotifications extends NotificationService {

    override def notifyAboutBuilds(builds: List[Build]) = {}

    override def notifyToggle(branch: Branch, build: Build) = {}
  }

}
