package controllers

import play.api.libs.json._
import models._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import models.PullRequestStatus
import models.Assignment
import models.BuildNode
import models.Branch
import models.Build

object Writes {
  implicit var buildNodeWrite: Writes[BuildNode] = null

  buildNodeWrite = (
    (__ \ "number").write[Int] ~
    (__ \ "name").write[String] ~
    (__ \ "status").writeNullable[String] ~
      (__ \ "statusUrl").write[String] ~
      (__ \ "artefactsUrl").write[String] ~
      (__ \ "children").lazyWriteNullable(traversableWrites[BuildNode](buildNodeWrite))
    )(unlift(BuildNode.unapply))

  implicit val buildWrite = Json.writes[Build]

  implicit val entityAssignment = Json.writes[Assignment]
  implicit val entityStateWrite = Json.writes[EntityState]
  implicit val entityWrite = Json.writes[Entity]
  implicit val prWrite = Json.writes[PullRequest]

  implicit val branchWrite = Json.writes[Branch]

  implicit val statusWrites = (
    (__ \ "isMergeable").write[Boolean] ~
      (__ \ "isMerged").write[Boolean])(unlift(PullRequestStatus.unapply))
}
