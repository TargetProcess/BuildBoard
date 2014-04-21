package controllers

import _root_.components.Registry
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import scala.util._
import models.github._


object Login extends Application {

  case class UserCredentials(username: String, password: String)


  val loginForm = Form[UserCredentials](
    mapping(
      "login" -> text,
      "password" -> text)(UserCredentials.apply)(UserCredentials.unapply))


  def index = Action {
    implicit request =>
      Ok(views.html.login(loginForm))
  }

  def logout = Action {
    implicit request =>
      Redirect(routes.Login.index()).withNewSession
  }

  def submit = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        login => {

          Registry.userRepository
            .authenticate(login.username, login.password) match {
            case Success((tpUser, token)) =>
              Registry.userRepository.save(tpUser, token)
              Redirect(routes.Landing.index()).withSession("login" -> tpUser.login)


            case Failure(e) =>
              play.Logger.error("Error during login", e)
              Ok(views.html.login(loginForm, Some("Something going wrong")))
          }
        })
  }

  def oauth(code: String) = IsAuthorized {
    user =>
      implicit request =>
        val (login, accessToken) = GithubApplication.login(code)
        Registry.userRepository.save(user.copy(githubLogin = login, githubToken = accessToken))
        Ok(views.html.closeWindow())
  }

  def githubLogin = IsAuthorized {
    user =>
      implicit request =>
        Ok(views.html.components.githubLogin.render(user, request))
  }
}
 
