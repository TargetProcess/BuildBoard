package controllers

import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models._
import models.tp.EntityRepo
import views.html.index


object Targetprocess extends Controller with Secured {
  implicit val statusWrites = (
    (__ \ "isMergeable").write[Boolean] ~
      (__ \ "isMerged").write[Boolean])(unlift(PullRequestStatus.unapply))

  def changeEntityState(entityId: Int, stateId: Int) = IsAuthorized {
    implicit user =>
      request =>
        val repo = new EntityRepo(user)
        implicit val writes = repo.entityStateWrite
        val newState = repo.changeEntityState(entityId, stateId)

        var text = views.html.components.entityState(entityId, newState).body
        Ok(Json.obj("text" -> text))
  }
}
