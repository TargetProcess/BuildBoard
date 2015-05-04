package components

import models.buildWatcher.{BuildWatcherComponentImpl, RerunRepositoryComponentImpl}
import models.configuration.ConfigComponentImpl
import models.github.GithubServiceComponentImpl
import models.jenkins.JenkinsServiceComponentImpl
import models.magicMerge.MagicMergeComponentImpl
import models.notifications.{NotificationComponentImpl, NotificationRepositoryComponentImpl}
import models.services.BranchServiceComponentImpl
import models.tp.{TargetprocessComponentImpl, UserRepositoryComponentImpl}
import models.{AuthInfo, BranchRepositoryComponentImpl, BuildRepositoryComponentImpl, User}

trait DefaultComponent
  extends AuthInfoProviderComponent
  with LoggedUserProviderComponent
  with GithubServiceComponentImpl
  with BranchServiceComponentImpl
  with TargetprocessComponentImpl
  with JenkinsServiceComponentImpl
  with BranchRepositoryComponentImpl
  with BuildRepositoryComponentImpl
  with NotificationComponentImpl
  with UserRepositoryComponentImpl
  with MagicMergeComponentImpl
  with BuildWatcherComponentImpl
  with NotificationRepositoryComponentImpl
  with RerunRepositoryComponentImpl
  with ConfigComponentImpl


class DefaultRegistry(val loggedUser: Option[User], val authInfo: AuthInfo) extends DefaultComponent {
  def this(user: User) = this(Some(user), user)

  def this(authInfo: AuthInfo) = this(None, authInfo)
}

object Registry extends UserRepositoryComponentImpl