package controllers

import play.api.mvc._
import play.api.libs.json.Json
import models.tp.EntityRepo
import Writes._

object Targetprocess extends Controller with Secured {

  def changeEntityState(entityId: Int, stateId: Int) = IsAuthorized {
    implicit user =>
      request =>
        val repo = new EntityRepo(user.token)
        val newState = repo.changeEntityState(entityId, stateId)


        Ok(Json.toJson(newState))
  }

}
