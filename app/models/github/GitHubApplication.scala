package models.github

import scalaj.http.Http
import scalaj.http.HttpOptions
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.UserService
import play.api.Play.current
import play.api.Play


object GithubApplication {
  private val config = Play.configuration.getConfig("github").get


  val clientId = config.getString("oauth.client_id").get
  val clientSecret = config.getString("oauth.client_secret").get
  val redirectUri = config.getString("oauth.redirect_url").get

  val user = config.getString("user").get
  val repo = config.getString("repo").get

  def url(branch: String) = s"https://github.com/$user/$repo/tree/$branch"

  def login(code: String) = {
    val req = Http.post("https://github.com/login/oauth/access_token")
      .params(
        "code" -> code,
        "client_id" -> GithubApplication.clientId,
        "client_secret" -> GithubApplication.clientSecret)
      .header("Accept", "application/xml")
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))

    val accessTokenXml = req.asXml
    val clientCode = accessTokenXml \ "access_token"
    val accessToken = clientCode.text

    val github = new GitHubClient().setOAuth2Token(accessToken)

    val userService = new UserService(github)
    (userService.getUser.getLogin, accessToken)
  }
}
