package controllers

import models.configuration._
import play.api.libs.json.{Format, JsError, JsSuccess, Json}
import Formats._

object Config extends Application {

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