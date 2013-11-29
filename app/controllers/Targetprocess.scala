package controllers

import play.api.mvc._


object Targetprocess extends Controller with Secured {
   /*
  def changeEntityState(entityId: Int, stateId: Int) = IsAuthorized {
    implicit user =>
      request =>
        val repo = new EntityRepo(user.token)
        implicit val writes = EntityRepo.entityStateWrite
        val newState = repo.changeEntityState(entityId, stateId)

        val text = views.html.components.entityState(entityId, newState).body
        Ok(Json.obj("text" -> text, "newState" -> newState))
  }
  */
}
