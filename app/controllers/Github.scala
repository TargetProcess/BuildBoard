package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import models.github.GitHubRepository
import controllers.Secured
import play.api.libs.json._
import play.api.libs.functional.syntax._

object Github extends Controller with Secured {
  implicit val statusWrites = (
    (__ \ "isMergeable").write[Boolean] ~
      (__ \ "isMerged").write[Boolean])(unlift(PullRequestStatus.unapply))

  def pullRequestStatus(id: Int) = IsAuthorized {
    implicit user =>
      request =>
        val status = new GitHubRepository().getPullRequestStatus(id)
        Ok(Json.toJson(status))
  }
}
