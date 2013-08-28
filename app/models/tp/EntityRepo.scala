package models.tp

import models.Login
import scalaj.http.Http
import TargetprocessApplication._
import scala.util.Try
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import models.EntityState
import models.Entity

object EntityRepo {

  def getUri(ids: List[Int]) = apiUri("Assignables") + "?format=json&take=1000&include=[Id,Name,EntityType[Name],EntityState[Id,Name,NextStates]]&where=Id%20in%20(" + ids.mkString(",") + ")"

  def getAssignables(ids: List[Int])(implicit user: Login) = {
    val uri = getUri(ids)

    val response = Http(uri)
      .auth(user.username, user.password)
      .asString
    val res = parseAssignables(response)

    res
  }

  private implicit var entityStateReads: Reads[EntityState] = null;
  entityStateReads = (
    (__ \ "Id").read[Int] ~
    (__ \ "Name").read[String] ~
    (__ \ "NextStates").readNullable(
      (__ \ "Items").lazyRead(list[EntityState](entityStateReads))))(EntityState)

  implicit val assignableReads = (
    (__ \ "Id").read[Int] ~
    (__ \ "Name").read[String] ~
    (__ \ "EntityType" \ "Name").read[String] ~
    (__ \ "EntityState").read[EntityState])(Entity)

  def parseEntityStates(response: String) = {
    val json = Json.parse(response)
    json.validate((__ \ "Items").read(list[EntityState])).get
  }

  def parseAssignables(response: String) = {
    val json = Json.parse(response)
    json.validate((__ \ "Items").read(list[Entity])).get
  }
}