package models.tp

import scalaj.http.{HttpOptions, Http}
import play.api.libs.json._
import scala.util._
import TargetprocessApplication._
import models.{AuthInfo, TpUser}
import components.{AuthInfoProviderComponent, UserRepositoryComponent}

trait UserRepositoryComponentImpl extends UserRepositoryComponent {

  def userRepository: UserRepository = new UserRepositoryImpl


  class UserRepositoryImpl extends UserRepository {

    def authenticate(username: String, password: String): Try[(TpUser, String)] = Try {
      val response = Http(apiUri("Authentication") + "?format=json")
        .auth(username, password)
        .option(HttpOptions.connTimeout(1000))
        .option(HttpOptions.readTimeout(5000))
        .asString

      val json = Json.parse(response)
      val tpToken = (json \ "Token").as[String]
      val user = new TargetprocessComponentImpl with AuthInfoProviderComponent {
        val authInfo: AuthInfo = new AuthInfo {
          val token = tpToken
          val githubToken = ""
        }
      }
        .entityRepository.getLoggedUser

      user
    }
  }

}

