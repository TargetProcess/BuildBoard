package models.configuration

import components.ConfigComponent
import models.buildActions.BuildParametersCategory
import models.cycles.Cycle
import play.api.Play
import play.api.Play.current
import play.api.libs.json._

import scala.concurrent.duration._

trait ConfigComponentImpl extends ConfigComponent {

  override val config = new ConfigService {

    implicit val teamRead = Json.reads[Team]
    implicit val buildParametersCategory = Json.reads[BuildParametersCategory]
    implicit val cycleParametersRead = Json.reads[CycleParameters]
    implicit val cycleConfigRead = Json.reads[CycleConfig]
    implicit val buildConfigRead = Json.reads[BuildConfig]
    implicit val buildBoardConfigRead = Json.reads[BuildBoardConfig]

    private def getString(path: String) = Play.configuration.getString(path).get

    override val jenkinsUrl: String = getString("jenkins.url")
    override val jenkinsDataPath = getString("jenkins.data.path")
    override val deployDirectoryRoot: String = getString("jenkins.data.deployPath")

    override val jenkinsInterval: FiniteDuration = Play.configuration.getMilliseconds("jenkins.cache.interval").getOrElse(60000L).milliseconds

    override def buildConfig: BuildBoardConfig = {
      val config = play.api.Play.getFile("conf/build.json")
      val source = scala.io.Source.fromFile(config)
      val json = try source.mkString finally source.close()
      Json.fromJson[BuildBoardConfig](Json.parse(json)) match {
        case JsSuccess(value, _) => value
        case JsError(errors) => println(errors); null
      }
    }
  }
}
