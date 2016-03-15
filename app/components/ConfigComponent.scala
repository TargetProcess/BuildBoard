package components

import models.teams.Team
import play.api.Configuration

import scala.concurrent.duration.FiniteDuration

trait ConfigComponent {
  val config: ConfigService

  trait ConfigService {
    val deployDirectoryRoot: String
    val jenkinsDataPath: String
    val jenkinsInterval: FiniteDuration
    val jenkinsUrl: String

    val buildConfig: BuildConfig
  }

}

trait BuildConfig {
  def getTestParts(category: String):List[String]

  def autoRerun(name: String): Boolean

  def getBuildConfig(name: String): Configuration

  def getTests(testName: String): List[String]

  def teams: List[Team]

  def unstableNodes: List[String]
}


