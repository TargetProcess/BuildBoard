package components

import models.{Build, Branch}
import models.teams.Team

import scala.util.Try

trait NotificationComponent {
  val notificationService: NotificationService

  trait NotificationService {
    def notifyAboutBuilds(builds: Iterator[Build])
    def notifyToggle(branch: Branch, build:Build)
    def notifyStartDeploy(team: Team, build:Build)
    def notifyDoneDeploy(team: Team, build:Build, result: Try[Any])
  }

}
