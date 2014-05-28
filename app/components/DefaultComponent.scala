package components

import models.github.GithubServiceComponentImpl
import models.services.BranchServiceComponentImpl
import models.tp.{UserRepositoryComponentImpl, TargetprocessComponentImpl}
import models.{AuthInfo, User, BuildRepositoryComponentImpl, BranchRepositoryComponentImpl}
import models.notifications.NotificationComponentImpl
import models.jenkins.JenkinsServiceComponentImpl
import models.magicMerge.MagicMergeComponentImpl

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


class DefaultRegistry(val loggedUser: Option[User], val authInfo:AuthInfo) extends DefaultComponent {
  def this(user:User) = this(Some(user), user)
  def this(authInfo:AuthInfo) = this(None, authInfo)
}

object Registry extends UserRepositoryComponentImpl