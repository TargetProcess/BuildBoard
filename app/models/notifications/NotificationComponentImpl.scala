package models.notifications

import com.mongodb.casbah.Imports._
import components._
import models._
import models.github.GithubStatus
import play.api.Play
import play.api.Play.current

import scala.collection.immutable.Iterable
import scala.util.{Failure, Success, Try}
import scalaj.http.{Http, HttpException, HttpOptions}
import models.teams.Team


trait NotificationComponentImpl extends NotificationComponent {

  this: NotificationComponentImpl
    with LoggedUserProviderComponent
    with UserRepositoryComponent
    with GithubServiceComponent
    with NotificationRepositoryComponent
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
          case models.BuildStatus.Unknown | models.BuildStatus.InProgress => "pending"
          case models.BuildStatus.Toggled | models.BuildStatus.Ok => "success"
          case models.BuildStatus.Failure => "error"
          case models.BuildStatus.Aborted | models.BuildStatus.TimedOut => "failure"
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

        val status = if (build.buildStatus == models.BuildStatus.Toggled) "green" else build.buildStatus.name

        val icon: String = getIcon(build)

        val text = s"$icon *${build.branch}* is toggled to $status by *${loggedUser.fold("Unknown")(_.fullName)}*. <${getBuildLink(build)}|#${build.number}>"

        post(text, broadcastChannel)

      }
      updateGithub(build)

    }


    override def notifyAboutBuilds(updatedBuilds: Iterator[Build]) = {

      val lastBuilds: Iterable[Build] = updatedBuilds.toStream.groupBy(_.branch)
        .map {
        case (_, builds) => builds.maxBy(_.number)
      }


      for (build <- lastBuilds) {

        val prevNotification = notificationRepository.getLastNotification(build.branch)

        prevNotification match {
          case Some(notification) =>
            if (notification.status != build.buildStatus.name) {
              sendNotification(build, Some(notification))
            }
          case None =>
            sendNotification(build, None)
        }

        notificationRepository.setLastNotification(build.branch, Notification(build.branch, build.buildStatus.name, build.timestamp))


      }
    }

    def needBroadcast(branch: String): Boolean = branch match {
      case BranchInfo.develop() => true
      case BranchInfo.hotfix(_) => true
      case BranchInfo.release(_) => true
      case _ => false
    }

    def sendNotification(currentBuild: Build, lastNotification: Option[Notification]) {
      val was = lastNotification.map(notification => s"(was *${notification.status}* at ${notification.timestamp.toString("HH:mm dd/MM")})").getOrElse("")

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

    def notifyStartDeploy(team: Team, build: Build) {
      post(s"Build <${getBuildLink(build)}> is starting to deploy for ${team.name}", team.channel)
    }

    def notifyDoneDeploy(team: Team, build: Build, result:Try[Any]) {
      val deployText = result match {
        case Success(_)=>"copied"
        case Failure(_)=>"not copied"
      }

      val message = s"Build <${getBuildLink(build)}> is $deployText for ${team.name}"

      post(message, team.channel)
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

    override def notifyAboutBuilds(builds: Iterator[Build]) = {}

    override def notifyToggle(branch: Branch, build: Build) = {}

    def notifyStartDeploy(team: Team, build: Build) = {
      play.Logger.info(s"StartDeploy $build -> $team")
    }

    def notifyDoneDeploy(team: Team, build: Build, result:Try[Any]) = {
      play.Logger.info(s"DoneDeploy $build -> $team, $result")
    }
  }

}
