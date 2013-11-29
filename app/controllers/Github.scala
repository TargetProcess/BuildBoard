package controllers

import play.api.mvc._
import models._
import models.github.GitHubRepository
import play.api.libs.json._
import Writes._

object Github extends Controller with Secured {


  def pullRequestStatus(id: Int) = IsAuthorized {
    implicit user =>
      request =>
        val status: PullRequestStatus = new GitHubRepository().getPullRequestStatus(id)
        Ok(Json.toJson(status))
  }
}
