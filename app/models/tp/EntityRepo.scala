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

object EntityRepo {

  def getUri(ids: List[Int]) = apiUri("Assignables") + "?format=json&take=1000&include=[Id,EntityState[Id,Name,NextStates]]&where=Id%20in%20(" + ids.mkString(",") + ")"

  def getAssignables(ids: List[Int])(implicit user: Login) = Try {
    val uri = getUri(ids)

    val response = Http(uri)
      .auth(user.username, user.password)
      .asString
    val res = parseEntityStates(response)

    res
  }

  private implicit var entityStateReads: Reads[EntityState] = null;
  entityStateReads = (
    (__ \ "Id").read[Int] ~
    (__ \ "Name").read[String] ~
    (__ \ "NextStates").readNullable(
      (__ \ "Items").lazyRead(list[EntityState](entityStateReads))))(EntityState)

  case class EntityStateCollection(entityStates: List[EntityState])

  def parseEntityStates(response: String) = {

    val entityStateCollectionReads = (
      (__ \ "Items").lazyRead(list[EntityState](entityStateReads)));

    val json = Json.parse(response)
    json.validate(entityStateCollectionReads).get
  }
}