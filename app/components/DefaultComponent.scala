package components

import models.github.GithubRepositoryComponentImpl
import models.services.BranchServiceComponentImpl
import models.tp.{UserRepositoryComponentImpl, TargetprocessComponentImpl}
import models.jenkins.JenkinsRepositoryComponentImpl
import models.{AuthInfo, User, BuildRepositoryComponentImpl, BranchRepositoryComponentImpl}
import models.notifications.NotificationComponentImpl

trait DefaultComponent
  extends AuthInfoProviderComponent
  with GithubRepositoryComponentImpl
  with BranchServiceComponentImpl
  with TargetprocessComponentImpl
  with JenkinsRepositoryComponentImpl
  with BranchRepositoryComponentImpl
  with BuildRepositoryComponentImpl
  with NotificationComponentImpl
  with UserRepositoryComponentImpl


class DefaultComponentImpl(user: User) extends DefaultComponent {
  override def authInfo: AuthInfo = user
}

object Registry extends UserRepositoryComponentImpl