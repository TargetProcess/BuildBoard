package controllers

import controllers.Github._
import models.services.CacheService
import play.api.libs.json.Json

import scala.util.{Failure, Success}

object Config extends Application {
  def getConfig = IsAuthorizedComponent {
    component =>
      implicit request =>
        Ok("")//component.config.buildConfig.toJson)
  }

}
