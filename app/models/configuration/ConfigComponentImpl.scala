package models.configuration

import java.io.{BufferedWriter, FileWriter}

import components.ConfigComponent
import models.Branch
import models.buildActions.BuildParametersCategory
import models.cycles.Cycle
import play.api.Play
import play.api.Play.current
import play.api.libs.json._

import scala.concurrent.duration._

trait ConfigComponentImpl extends ConfigComponent {

  import controllers.Writes._

  override val config = new ConfigService {

    private def getString(path: String) = Play.configuration.getString(path).get

    override val jenkinsUrl: String = getString("jenkins.url")
    override val jenkinsDataPath = getString("jenkins.data.path")
    override val deployDirectoryRoot: String = getString("jenkins.data.deployPath")

    override val jenkinsInterval: FiniteDuration = Play.configuration.getMilliseconds("jenkins.cache.interval").getOrElse(60000L).milliseconds

    val buildConfigPath = getString("build.config.path")

    override def saveBuildConfig(buildConfig: BuildBoardConfig): Unit = {
      val file = play.api.Play.getFile(buildConfigPath)
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(Json.prettyPrint(Json.toJson(buildConfig)))
      bw.close()
    }

    override def buildConfig: BuildBoardConfig = {
      val config = play.api.Play.getFile(buildConfigPath)
      val source = scala.io.Source.fromFile(config)
      val json = try source.mkString finally source.close()
      Json.fromJson[BuildBoardConfig](Json.parse(json)) match {
        case JsSuccess(value, _) => value
        case JsError(errors) => println(errors); null
      }
    }
  }
}
