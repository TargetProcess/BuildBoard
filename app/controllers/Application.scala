package controllers

import models.github.GitHubRepository
import play.api.mvc._
import models._
import play.api.mvc._
import models._
import models.github.GitHubRepository
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.tp.EntityRepo._


object Application extends Controller with Secured {

  /*
   */
  implicit val buildWrite = Json.writes[Build]
  implicit val entityAssignment = Json.writes[Assignment]
  implicit val entityWrite = Json.writes[Entity]
  implicit val prWrite = Json.writes[PullRequest]
  implicit val branchWrite = Json.writes[Branch]

  def index = {
    IsAuthorized {
      implicit user =>
        implicit request => Ok(views.html.index(""))
    }

  }

  def branches = IsAuthorized {
    implicit user =>
      implicit request =>
        val branches: List[Branch] = new GitHubRepository().getBranches

        Ok(Json.toJson(branches))
  }
}
