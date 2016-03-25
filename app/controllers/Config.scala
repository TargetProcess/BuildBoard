package controllers

import models.configuration._
import play.api.libs.json.{Format, JsError, JsSuccess, Json}


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
          json.validate[BuildBoardConfig] match {
            case JsSuccess(config, _) => component.config.saveBuildConfig(config); Ok("json")
            case JsError(errors) => BadRequest("invalid config")
          }
        }
          .getOrElse(BadRequest("Expecting Json data"))
  }
}