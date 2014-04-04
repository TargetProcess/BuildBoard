package controllers

import play.api.mvc._
import play.api.libs.json._
import Writes._
import models.BranchRepository

object Branches extends Application {

  def branches = IsAuthorized {
    user =>
      implicit request =>
        val branches = new BranchRepository().getBranches

        Ok(Json.toJson(branches))
  }
}
