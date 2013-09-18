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
import scalaj.http.Http.Request
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit._

trait EntityRepository {
  def changeEntityState(entityId: Int, stateId: Int): EntityState

  def getAssignables(ids: List[Int]): List[Entity]

  def getLoggedUser: (TpUser, String)
}

class EntityRepo(token: String) extends EntityRepository {
  val stateSelector = "EntityState[Id,IsFinal,Role,Name,NextStates]"

  private implicit var entityStateReads: Reads[EntityState] = null
  entityStateReads = (
    (__ \ "Id").read[Int] ~
      (__ \ "Name").read[String] ~
      (__ \ "IsFinal").readNullable[Boolean] ~
      (__ \ "Role").readNullable(
        (__ \ "Name").read[String]) ~
      (__ \ "NextStates").readNullable(
        (__ \ "Items").lazyRead(list[EntityState](entityStateReads))))(EntityState)

  implicit val entityStateWrite: Writes[EntityState] =
    (
      (__ \ "Id").write[Int] ~
        (__ \ "Name").write[String] ~
        (__ \ "Role").writeNullable(
          (__ \ "Name").write[String]) ~
        (__ \ "IsFinal").write[Option[Boolean]] ~
        (__ \ "NextStates").lazyWriteNullable(Writes.traversableWrites[EntityState](entityStateWrite))
      )(e => (e.id, e.name, e.role, e.isFinalOpt, e.nextStates))

  implicit val assignmentReads = (
    (__ \ "Role" \ "Name").read[String] ~
      (__ \ "GeneralUser" \ "Id").read[Int] ~
      (__ \ "GeneralUser" \ "AvatarUri").read[String] ~
      (__ \ "GeneralUser" \ "FirstName").read[String] ~
      (__ \ "GeneralUser" \ "LastName").read[String])((role, id, avatar, firstName, lastName) => Assignment(id, role, avatar, firstName, lastName))

  implicit val assignableReads = (
    (__ \ "Id").read[Int] ~
      (__ \ "Name").read[String] ~
      (__ \ "EntityType" \ "Name").read[String] ~
      (__ \ "EntityState").read[EntityState] ~
      (__ \ "Assignments").readNullable(
        (__ \ "Items").read(list[Assignment])))(Entity)


  implicit val loggedUser = (

    (__ \ "Id").read[Int] ~
      (__ \ "Email").read[String] ~
      (__ \ "FirstName").read[String] ~
      (__ \ "LastName").read[String]

    )((id, login, firstName, lastName) => TpUser(id, login, firstName + " " + lastName))

  private def sendRequest(request: Request) = {
    val toSend = request
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))
      .asString

    Json.parse(toSend)
  }

  private def post(root: String, include: String, data: JsValue, id: Option[Int] = None) = {
    val uri = apiUri(root) + "/" + id.map(_.toString).getOrElse("") + s"?format=json&token=$token&include=$include"
    val request = Http.postData(uri, Json.stringify(data))
      .header("content-type", "application/json")

    sendRequest(request)
  }

  private def get(root: String, include: String, where: String = "", id: Option[Int] = None) = {
    val uri = apiUri(root) + "/" + id.map(_.toString).getOrElse("") + s"?token=$token&format=json&include=$include&where=$where"
    val request = Http(uri)

    sendRequest(request)
  }

  private def split(ints: List[Int], count: Int): List[List[Int]] = ints match {
    case Nil => Nil
    case l => {
      val (head, tail) = (ints.take(count), ints.drop(count))
      head :: split(tail, count)
    }
  }

  def changeEntityState(entityId: Int, stateId: Int) = {
    val include = s"[$stateSelector]"

    val data = Json.obj(
      "id" -> entityId,
      "entityState" -> Json.obj(
        "id" -> stateId
      )
    )

    val json = post("Assignables", include, data)

    val value = json.validate((__ \ "EntityState").read[EntityState])
    value.get
  }

  def getAssignables(ids: List[Int]): List[Entity] = {
    val include = s"[Id,Name,Assignments[GeneralUser[Id,AvatarUri,FirstName,LastName],Role[Name]],EntityType[Name],$stateSelector]"

    val idGroups: List[List[Int]] = split(ids, 20)

    idGroups.flatMap {
      group =>
        val json = get("Assignables", include, where = "Id%20in%20(" + group.mkString(",") + ")")
        json.validate((__ \ "Items").read(list[Entity])).get
    }

  }

  def getLoggedUser: (TpUser, String) = {
    val json = get("Context", "")

    val value = json.validate((__ \ "LoggedUser").read[TpUser])
    (value.get, token)
  }
}