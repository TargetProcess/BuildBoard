package controllers

import play.api.mvc._
import models.github.{ Branch, GitHubRepository }
import models.User
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import models._
import views._

object Application extends Controller with Secured {

  def index = IsAuthorized {
    user =>
      implicit request =>
        val data: Iterable[Branch] = if (user.githubToken != null)
          new GitHubRepository(user.githubToken).getBranches
        else
          Iterable()
        Ok(views.html.index(data, user))
  }
}
