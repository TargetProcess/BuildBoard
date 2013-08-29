package controllers

import models.github.GitHubRepository
import play.api.mvc._
import models._

object Application extends Controller with Secured {

  def index = branches(true)
  
  def branches(all: Boolean = true) = IsAuthorized {
    implicit user =>
      implicit request =>
        val branches = new GitHubRepository().getBranches

        val data = if (all) branches else branches.filter(x=>x.entity.isDefined)

        Ok(views.html.index(data, user))
  }

  def branch(name:String) = IsAuthorized {
    implicit user =>
      request =>
       val branch = new GitHubRepository().getBranch(name)
        Ok("")
  }
}
