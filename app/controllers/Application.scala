package controllers

import play.api.mvc._
import models.{Branch, GitHubRepository}
import models.User
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import models._
import views._ 

object Application extends Controller with Secured {

 def index = IsAuthenticated { username => implicit request =>
    User.findByEmail(username).map { user =>
      val data: Iterable[Branch] = request.session.get("github.token") match {
        case Some(token: String) =>
          new GitHubRepository(token).getBranches
        case None => Iterable()
      }
      Ok(views.html.index(data))
    }.getOrElse(Forbidden)
  }
}
