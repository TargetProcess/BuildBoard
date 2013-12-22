package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import Writes._
import models.PullRequestStatus

object Github extends Controller with Secured {


  def pullRequestStatus(id: Int) = IsAuthorized {
    implicit user =>
      request =>
        val status: PullRequestStatus = new BranchesRepository().getPullRequestStatus(id)
        Ok(Json.toJson(status))
  }
}
