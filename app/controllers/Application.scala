package controllers

import play.api.mvc._
import models.GitHub.{ Branch, GitHubRepository }
import models.User
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import anorm._
import models._
import views._
import models.github.GitHubRepository

object Application extends Controller with Secured {

  def index = IsAuthorized {
    implicit user =>
      implicit request =>
        val data: Iterable[Branch] = if (user.githubToken != null)
          new GitHubRepository().getBranches
        else
          Iterable()
        Ok(views.html.index(data, user))
  }
}
