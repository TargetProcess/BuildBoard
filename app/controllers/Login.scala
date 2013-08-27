package controllers

import play.api.mvc.{Cookie, Action, Controller}
import scalaj.http.{HttpOptions, Http, Token}
import org.kohsuke.github.GitHub

object Login extends Controller {
  val consumer = Token("72b7f38d644d0a1330f7", "dec66c9b3f49f0b5eba70fd163de03d5d76ce220")


  def index = Action {
    implicit request =>
      Ok(views.html.login(consumer.key))
  }

  def oauth(code: String) = Action {

    val req = Http.post("https://github.com/login/oauth/access_token")
      .params(
      "code" -> code,
      "client_id" -> consumer.key,
      "client_secret" -> consumer.secret
    )
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
      .withSession("github.token" -> accessToken, "github.login" -> login)
      .withCookies(Cookie("github", "token.ready"))
  }
}
