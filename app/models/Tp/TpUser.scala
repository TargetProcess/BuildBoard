package models.tp

import scalaj.http.Http
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.util._

trait TpUserRepo {

  def authenticate(username: String, password: String) = Try {
    val response = Http("http://plan.tpondemand.com/api/v1/Context?format=json")
      .auth(username, password)
      .asString

    val json = Json.parse(response);
    val jsValue = json \ "LoggedUser" \ "Id";
    
    val optUser = jsValue.asOpt[Int].map(TpUser(_, username)) 
    
    optUser.get
  }  
}

case class TpUser(id: Int, login: String)

