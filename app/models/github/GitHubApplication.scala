package models.github

import scalaj.http.Http
import scalaj.http.HttpOptions
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.UserService

object GitHubApplication {
  val clientId = "72b7f38d644d0a1330f7"
  val clientSecret = "dec66c9b3f49f0b5eba70fd163de03d5d76ce220"
    
   def login(code:String) = { 
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

     val github =  new GitHubClient().setOAuth2Token(accessToken)

     val userService = new UserService(github)
     (userService.getUser.getLogin, accessToken)
  }
}
