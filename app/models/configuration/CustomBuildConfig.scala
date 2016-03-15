package models.configuration

import com.typesafe.config.ConfigFactory
import components.BuildConfig
import models.teams.Team
import play.api.Configuration
import play.api.Play.current
import scala.collection.JavaConverters._

class CustomBuildConfig extends BuildConfig {

  def config = play.api.Configuration(ConfigFactory.parseFileAnySyntax(play.api.Play.getFile("conf/build.json")))


  override def teams: List[Team] = (for (
    teamConfig <- config.getConfigList("teams").get.asScala;
    name <- teamConfig.getString("name");
    deployTo <- teamConfig.getString("deployTo");
    channel <- teamConfig.getString("channel")
  ) yield Team(name, channel, deployTo)).toList

  override def unstableNodes: List[String] = config.getStringList("build.unstableNodes").get.asScala.toList

  override def getTests(testName: String): List[String] = config.getConfig("build").get.getStringList(testName).get.asScala.toList.distinct

  override def getBuildConfig(name: String): Configuration = {
    config.getConfig(s"build.cycle.$name").get
  }

  override def autoRerun(name: String): Boolean = config.getConfig("autoRerun").flatMap(_.getBoolean(name)).getOrElse(false)

  override def getTestParts(category: String): List[String] = config.getConfig("build")
    .flatMap(_.getStringList(category))
    .map(_.asScala.toList)
    .getOrElse(Nil)
}
