package controllers

import play.api.mvc._
import models.{Branch, GitHubRepository}

object Application extends Controller {

  def index() = Action {
    implicit request =>
      val data: Iterable[Branch] = request.session.get("github.token") match {
        case Some(token: String) =>
          new GitHubRepository(token).getBranches
        case None => Iterable()
      }
      Ok(views.html.index(data))
  }

}
