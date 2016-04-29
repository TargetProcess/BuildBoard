package controllers

import play.api.libs.json.Json
import Formats._

object Targetprocess extends Application {

  def changeEntityState(entityId: Int, stateId: Int) = AuthorizedComponent {
    component =>
      request =>

        val newState = component.entityRepository.changeEntityState(entityId, stateId)


        Ok(Json.toJson(newState))
  }

}
