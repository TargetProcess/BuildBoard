package controllers

import play.api._
import play.api.mvc._
import org.kohsuke.github.GitHub

object Application extends Controller {

  def index(message: String) = Action {
    implicit request =>
      val data: Array[String] = request.session.get("github") match {
        case Some(token: String) => {
          val github = GitHub.connectUsingOAuth(token)
          val repo = github.getRepository("TargetProcess/TP")
          val branches = repo.getBranches
          Array()
        }
        case None => Array()
      }
      Ok(views.html.index(message, data))
  }

}
