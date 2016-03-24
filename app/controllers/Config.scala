package controllers

import models.configuration._
import play.api.libs.json.{Format, JsSuccess, Json}


object Config extends Application {
  implicit val team = Json.format[DeployConfig]
  implicit val cycleParameters = Json.format[CycleParameters]
  implicit val cycleConfig = Json.format[CycleConfig]
  implicit val buildConfig = Json.format[BuildConfig]
  implicit val buildBoardConfig: Format[BuildBoardConfig] = Json.format[BuildBoardConfig]

  def getConfig = AuthorizedComponent {
    component =>
      implicit request =>
        Ok(Json.toJson(component.config.buildConfig))
  }

  def setConfig() = AuthorizedComponent {
    component =>
      implicit request =>
        request.body.asJson.map { json =>
          val config = json.as[BuildBoardConfig]
          component.config.saveBuildConfig(config)
          Ok("Saved")
        }.getOrElse {
          BadRequest("Expecting Json data")
        }
  }
}