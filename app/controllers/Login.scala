package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import views._
import scala.util._
import models.github._

object Login extends Controller with Secured {

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
      Redirect(routes.Login.index).withNewSession
  }

  def submit = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        login =>
          User.authenticate(login.username, login.password) match {
            case Success(tpUser) => {

              User.saveLogged(tpUser, login)

              Redirect(routes.Application.index).withSession("login" -> login.username)
            }
            case Failure(e) => Ok(views.html.login(loginForm))
          })
  }

  def oauth(code: String) = IsAuthorized {
    user =>
      implicit request =>
        val (login, accessToken) = GitHubApplication.login(code)
        User.save(user.copy(githubLogin = login, githubToken=accessToken))       
        Ok(views.html.closeWindow())
  }
}
 
