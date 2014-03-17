package models.tp

import scalaj.http.{HttpOptions, Http}
import play.api.libs.json._
import scala.util._
import TargetprocessApplication._

trait TpUserRepo {

  def authenticate(username: String, password: String) = Try {
    val response = Http(apiUri("Authentication") + "?format=json")
      .auth(username, password)
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))
      .asString

    val json = Json.parse(response)
    val token = (json \ "Token").as[String]
    val user = new EntityRepo(token).getLoggedUser

    user
  }
}

case class TpUser(id: Int, login: String, fullName: String)

