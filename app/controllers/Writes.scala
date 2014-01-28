package controllers

import play.api.libs.json._
import models._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import org.joda.time.DateTime

object Writes {
  implicit var buildNodeWrite: Writes[BuildNode] = null

  buildNodeWrite = (
    (__ \ "name").write[String] ~
    (__ \ "runName").write[String] ~
    (__ \ "status").writeNullable[String] ~
      (__ \ "statusUrl").write[String] ~
      (__ \ "artifactsUrl").writeNullable[String] ~
      (__ \ "timestamp").write[DateTime] ~
      (__ \ "children").lazyWrite(traversableWrites[BuildNode](buildNodeWrite))
    )(unlift(BuildNode.unapply))

  implicit val buildWrite = Json.writes[Build]

  implicit val buildActionWrite = (
    (__ \ "name").write[String] ~
      (__ \ "pullRequestId").writeNullable[Int] ~
      (__ \ "branchId").writeNullable[String] ~
      (__ \ "fullCycle").write[Boolean]
    )(unlift(BuildAction.unapply))

  implicit val entityAssignment = Json.writes[Assignment]
  implicit val entityStateWrite = Json.writes[EntityState]
  implicit val entityWrite = Json.writes[Entity]
  implicit val prWrite = Json.writes[PullRequest]

  implicit val branchWrite = Json.writes[Branch]

  implicit val statusWrites = (
    (__ \ "isMergeable").write[Boolean] ~
      (__ \ "isMerged").write[Boolean])(unlift(PullRequestStatus.unapply))
}
