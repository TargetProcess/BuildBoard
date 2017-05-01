package components

import models.buildRerun.{AutoBuildRerunComponentImpl, RerunRepositoryComponentImpl}
import models.buildWatcher.NotificationBuildWatcherComponentImpl
import models.configuration.ConfigComponentImpl
import models.cycles.ConfigurableCycleBuilderComponentImpl
import models.deploy.DeployServiceComponentImpl
import models.github.GithubServiceComponentImpl
import models.jenkins.JenkinsServiceComponentImpl
import models.jenkins.ForceBuildComponentImpl
import models.magicMerge.MagicMergeComponentImpl
import models.notifications.{NotificationComponentImpl, NotificationRepositoryComponentImpl}
import models.services.BranchServiceComponentImpl
import models.tp.{TargetprocessComponentImpl, UserRepositoryComponentImpl}
import models._
import play.api.Play
import play.api.Play.current

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
  with AutoBuildRerunComponentImpl
  with NotificationRepositoryComponentImpl
  with RerunRepositoryComponentImpl
  with ConfigComponentImpl
  with NotificationBuildWatcherComponentImpl
  with ConfigurableCycleBuilderComponentImpl
  with DeployServiceComponentImpl
  with ForceBuildComponentImpl
  with JobRunRepositoryComponentImpl


class DefaultRegistry(val loggedUser: Option[User], val authInfo: AuthInfo) extends DefaultComponent {
  def this(user: User) = this(Some(user), user)

  def this(authInfo: AuthInfo) = this(None, authInfo)
}

object DefaultRegistry extends DefaultRegistry( new AuthInfo {
  override val token: String = Play.configuration.getString("cache.user.tp.token").get
  override val githubToken: String = Play.configuration.getString("cache.user.github.token").get
})

object Registry extends UserRepositoryComponentImpl