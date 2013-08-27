package controllers

import play.api.mvc.{ Cookie, Action, Controller }
import scalaj.http.{ HttpOptions, Http, Token }
import org.kohsuke.github.GitHub
import models.GitHubApplication
import play.api.data._
import play.api.data.Forms._
import models.User
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import views._
import scala.util._

object Login extends Controller {

  val loginForm = Form[UserCredentials](
    mapping(
      "login" -> text,
      "password" -> text)(UserCredentials.apply)(UserCredentials.unapply))

  def index = Action {
    implicit request =>
      Ok(views.html.login(loginForm))
  }

  def submit = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        login =>
          User.authenticate(login.username, login.password) match {
            case Success(tpUser) =>{
              
              val userFromDb = User.findOneById(tpUser.id)
              val newUser = 
              userFromDb match {
                case None => {
                  User(tpId=tpUser.id, username=login.username, password=login.password)
                }
                case Some(user) => {
                  user.copy(username=login.username, password=login.password)
                }
              }
              User.save(newUser)
              
              Redirect(routes.Application.index).withSession("login" -> login.username)
            } 
            case Failure(e) => Ok(views.html.login(loginForm))
          })
  }

  def oauth(code: String) = Action { implicit request =>

    val req = Http.post("https://github.com/login/oauth/access_token")
      .params(
        "code" -> code,
        "client_id" -> GitHubApplication.clientId,
        "client_secret" -> GitHubApplication.clientSecret)
      .header("Accept", "application/xml")
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))

    val accessTokenXml = req.asXml
    val clientCode = accessTokenXml \ "access_token"
    val accessToken = clientCode.text

    val github = GitHub.connectUsingOAuth(accessToken)
    val user = github.getMyself
    val login = user.getLogin

    Ok(views.html.closeWindow())
      .withSession(
        session + ("github.token" -> accessToken)
          + ("github.login" -> login))
      .withCookies(Cookie("github", "token.ready"))
  }
}
 
