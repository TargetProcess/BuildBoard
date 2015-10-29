package models.configuration

import components.ConfigComponent
import models.teams.Team
import play.api.Play
import play.api.Play.current
import scala.collection.JavaConverters._
import scala.concurrent.duration._
trait ConfigComponentImpl extends ConfigComponent {

  override val config = new ConfigService {

    override val jenkinsUrl: String = getString("jenkins.url")
    override val jenkinsDataPath = getString("jenkins.data.path")
    override val deployDirectoryRoot: String = getString("jenkins.data.deployPath")

    override val teams: List[Team] = (for (
      teamConfig <- Play.configuration.getConfigList("teams").get.asScala;
      name <- teamConfig.getString("name");
      deployTo <- teamConfig.getString("deployTo");
      channel <- teamConfig.getString("channel")
    ) yield Team(name, channel, deployTo)).toList

    private def getString(path: String) = Play.configuration.getString(path).get

    override val unstableNodes: List[String] = Play.configuration.getStringList("build.unstableNodes").get.asScala.toList
    override val jenkinsInterval: FiniteDuration =  Play.configuration.getMilliseconds("jenkins.cache.interval").getOrElse(60000L).milliseconds
  }
}
