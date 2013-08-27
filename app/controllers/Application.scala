package controllers

import play.api.mvc._
import models.{ Branch, GitHubRepository }
import models.User
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import models._
import views._

object Application extends Controller with Secured {

  def index = IsAuthenticated {
    username =>
      implicit request =>

        User.findOneByUsername(username).map { user =>
          val data: Iterable[Branch] = if (user.githubToken != null)
            new GitHubRepository(user.githubToken).getBranches
          else
            Iterable()
          
            Ok(views.html.index(data))
        }.getOrElse(Forbidden)
  }
}
