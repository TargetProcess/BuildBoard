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

object Login extends Controller {

  val loginForm = Form(
    mapping(
      "login" -> text,
      "password" -> text,
      "accept" -> boolean)((login, password, _) => User(login, password))((user: User) => Some(user.login, user.password, false)))

  def index = Action {
    implicit request =>
      Ok(views.html.login(loginForm))
  }

  def submit = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        user => Redirect(routes.Application.index).withSession("login" -> user.login))
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
        session + ("github.token" -> accessToken) + ("github.login" -> login))
      .withCookies(Cookie("github", "token.ready"))
  }
}

trait Secured {

  private def username(request: RequestHeader) = request.session.get("login")
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Login.index)

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) =
    Security.Authenticated(username, onUnauthorized) {
      user => Action(request => f(user)(request))
    }
}
 
