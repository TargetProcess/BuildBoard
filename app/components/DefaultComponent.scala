package components

import models.github.GithubRepositoryComponentImpl
import models.services.BranchServiceComponentImpl
import models.tp.{UserRepositoryComponentImpl, TargetprocessComponentImpl}
import models.jenkins.JenkinsRepositoryComponentImpl
import models.{AuthInfo, User, BuildRepositoryComponentImpl, BranchRepositoryComponentImpl}
import models.notifications.NotificationComponentImpl

trait DefaultComponent
  extends AuthInfoProviderComponent
  with LoggedUserProviderComponent
  with GithubRepositoryComponentImpl
  with BranchServiceComponentImpl
  with TargetprocessComponentImpl
  with JenkinsRepositoryComponentImpl
  with BranchRepositoryComponentImpl
  with BuildRepositoryComponentImpl
  with NotificationComponentImpl
  with UserRepositoryComponentImpl


class DefaultRegistry(val loggedUser: Option[User], val authInfo:AuthInfo) extends DefaultComponent {
  def this(user:User) = this(Some(user), user)
  def this(authInfo:AuthInfo) = this(None, authInfo)
}

object Registry extends UserRepositoryComponentImpl