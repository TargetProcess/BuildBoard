package models.tp

import models.Login
import scalaj.http.Http
import TargetprocessApplication._
import scala.util.Try
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

object EntityRepo {

  implicit val entityStateReads:Reads[EntityState] = (
      (__ \ "Id").read[Int] ~
      (__ \ "Name").read[String] ~
      (__ \ "NextStates" \ "Items").read[List[EntityState]]
  )(EntityState)
  
  implicit val assignableReads = (
      (__ \ "Id").read[Int] ~
      (__ \ "Name").read[String] ~
      (__ \ "EntityState").read[EntityState]
  )(Assignable)

  
  def getUri(ids:List[Int]) = apiUri("Assignables") + "?format=json&take=1000&include=[Id,EntityState[Id,Name,NextStates]]&where=Id%20in%20("+ids.mkString(",")+")" 
  
  def getAssignables(ids: List[Int])(implicit user: Login) = Try {
    val uri = getUri(ids) 
    
    val response = Http(uri)
      .auth(user.username, user.password)
      .asString
      
      
      
    val json = Json.parse(response)
    
    val res = (json\"Items").as[Assignable]
    
    res
  }
}

case class AssignableList(assignables:List[Assignable])
case class Assignable(id:Int, name:String, entityState:EntityState)
case class EntityState(id:Int, name:String, nextStates:List[EntityState]=Nil)