package controllers

import models.github.GitHubRepository
import play.api.mvc._
import models._

object Application extends Controller with Secured {

  def index = branches(true)
  
  def branches(all: Boolean = true) = IsAuthorized {
    implicit user =>
      implicit request =>
        val branches = if (user.githubToken != null)
          new GitHubRepository().getBranches
        else
          Iterable()

        val data = if (all) branches else branches.filter(x=>x.entity.isDefined)

        Ok(views.html.index(data, user))
  }
}
