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
import models.Assignment
import scalaj.http.HttpOptions

object EntityRepo {

  def getUri(ids: List[Int]) = apiUri("Assignables") +
    "?format=json&take=1000&" +
    "include=[Id,Name,Assignments[GeneralUser[AvatarUri,FirstName,LastName],Role[Name]],EntityType[Name],EntityState[Id,IsFinal,Role,Name,NextStates]]&where=Id%20in%20(" + ids.mkString(",") + ")"

  def getAssignables(ids: List[Int])(implicit user: Login) = {
    val uri = getUri(ids)

    val response = Http(uri)
      .auth(user.username, user.password)
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))
      .asString
    val res = parseAssignables(response)

    res
  }

  private implicit var entityStateReads: Reads[EntityState] = null
  entityStateReads = (
    (__ \ "Id").read[Int] ~
      (__ \ "Name").read[String] ~
      (__ \ "IsFinal").readNullable[Boolean] ~
      (__ \ "Role").readNullable(
        (__ \ "Name").read[String]) ~
      (__ \ "NextStates").readNullable(
        (__ \ "Items").lazyRead(list[EntityState](entityStateReads))))(EntityState)

  implicit val assignmentReads = (
    (__ \ "Role" \ "Name").read[String] ~
      (__ \ "GeneralUser" \ "AvatarUri").read[String] ~
      (__ \ "GeneralUser" \ "FirstName").read[String] ~
      (__ \ "GeneralUser" \ "LastName").read[String])((role, avatar, firstName, lastName) => Assignment(role, avatar, firstName, lastName))

  implicit val assignableReads = (
    (__ \ "Id").read[Int] ~
      (__ \ "Name").read[String] ~
      (__ \ "EntityType" \ "Name").read[String] ~
      (__ \ "EntityState").read[EntityState] ~
      (__ \ "Assignments").readNullable(
        (__ \ "Items").read(list[Assignment])))(Entity)

  def parseEntityStates(response: String) = {
    val json = Json.parse(response)
    json.validate((__ \ "Items").read(list[EntityState]))
  }

  def parseAssignables(response: String) = {
    val json = Json.parse(response)
    json.validate((__ \ "Items").read(list[Entity]))
  }
}