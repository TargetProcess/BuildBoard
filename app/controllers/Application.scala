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
import Writes._


object Application extends Controller with Secured {

  def index = {
    IsAuthorized {
      implicit user =>
        implicit request => Ok(views.html.index(user))
    }

  }

  def branches = IsAuthorized {
    implicit user =>
      implicit request =>
        val branches: List[Branch] = new GitHubRepository().getBranches

        Ok(Json.toJson(branches))
  }


  def branch(id:String) = IsAuthorized {
      implicit user =>
        implicit request =>
          val branch: Branch = new GitHubRepository().getBranch(id)

          Ok(Json.toJson(branch))
  }
}
