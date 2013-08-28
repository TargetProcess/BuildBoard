package models.tp

import scalaj.http.Http
import play.api.libs.json._
import scala.util._
import TargetprocessApplication._

trait TpUserRepo {

  def authenticate(username: String, password: String) = Try {
    val response = Http(apiUri("Context") + "?format=json")
      .auth(username, password)
      .asString

    val json = Json.parse(response);

    val user =TpUser(
        (json \ "LoggedUser" \ "Id").as[Int], 
        username,
        (json \ "LoggedUser" \ "FirstName").as[String] + " " + (json \ "LoggedUser" \ "LastName").as[String] 
        
    )

    user
  }
}

case class TpUser(id: Int, login: String, fullName: String)

