package controllers

import models.github.GitHubRepository
import play.api.mvc._
import models._

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
