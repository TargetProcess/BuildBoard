package models.tp

import scalaj.http.Http
import play.api.libs.json._
import scala.util._
import TargetprocessApplication._

trait TpUserRepo {

  def authenticate(username: String, password: String) = Try {
    val response = Http(apiUri("Context")+"?format=json")
      .auth(username, password)
      .asString

    val json = Json.parse(response);
    val jsValue = json \ "LoggedUser" \ "Id";
    
    val optUser = jsValue.asOpt[Int].map(TpUser(_, username)) 
    
    optUser.get
  }  
}

case class TpUser(id: Int, login: String)

