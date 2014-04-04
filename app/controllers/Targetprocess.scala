package controllers

import play.api.mvc._
import play.api.libs.json.Json
import Writes._
import models.services.CacheService

object Targetprocess extends Application {

  def changeEntityState(entityId: Int, stateId: Int) = IsAuthorized {
    user =>
      request =>
        val component = new components.Default {
          val authInfo = user
        }

        val newState = component.entityRepository.changeEntityState(entityId, stateId)


        Ok(Json.toJson(newState))
  }

}
