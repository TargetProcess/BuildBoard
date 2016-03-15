package models.configuration

import components.{BuildConfig, ConfigComponent}
import play.api.Play
import play.api.Play.current

import scala.concurrent.duration._
trait ConfigComponentImpl extends ConfigComponent {

  override val config = new ConfigService {
    private def getString(path: String) = Play.configuration.getString(path).get

    override val jenkinsUrl: String = getString("jenkins.url")
    override val jenkinsDataPath = getString("jenkins.data.path")
    override val deployDirectoryRoot: String = getString("jenkins.data.deployPath")

    override val jenkinsInterval: FiniteDuration = Play.configuration.getMilliseconds("jenkins.cache.interval").getOrElse(60000L).milliseconds
    override val buildConfig: BuildConfig = new CustomBuildConfig
  }
}
