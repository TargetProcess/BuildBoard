package components

import models.Branch
import models.configuration.BuildBoardConfig

import scala.concurrent.duration.FiniteDuration

trait ConfigComponent {
  val config: ConfigService

  trait ConfigService {
    val deployDirectoryRoot: String
    val jenkinsDataPath: String
    val jenkinsInterval: FiniteDuration
    val jenkinsUrl: String

    def buildConfig: BuildBoardConfig
    def saveBuildConfig(buildConfig: BuildBoardConfig)
  }
}



