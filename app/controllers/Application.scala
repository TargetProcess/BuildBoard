package controllers

import play.api.mvc._
import play.api.libs.json._
import Writes._
import models.BranchRepository

object Application extends Controller with Secured {

  def index = {
    IsAuthorized {
      user =>
        implicit request => Ok(views.html.index(user))
    }
  }

  def branches = IsAuthorized {
    user =>
      implicit request =>
        val branches = new BranchRepository().getBranches

        Ok(Json.toJson(branches))
  }
}
