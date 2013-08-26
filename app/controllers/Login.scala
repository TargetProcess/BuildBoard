package controllers

import play.api.mvc.{Action, Controller}
import scalaj.http.{Http, Token}

object Login extends Controller {
  val consumer = Token("72b7f38d644d0a1330f7", "dec66c9b3f49f0b5eba70fd163de03d5d76ce220")


  def index = Action { implicit request =>
    Ok(views.html.login(consumer.key))
  }

  def oauth(code: String) = Action {
    implicit request =>

      val accessToken = Http("http://github.com/oauth/access_token").proxy("http://fomin", 8888).oauth(consumer,None,Some(code)).asString

      Redirect(controllers.routes.Application.index(accessToken))
  }
}
