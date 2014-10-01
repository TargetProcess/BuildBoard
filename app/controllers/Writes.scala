package controllers

import models.buildActions.{BuildAction, BuildParametersCategory}
import play.api.libs.json._
import models._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import models.PullRequestStatus
import components.MagicMergeResult

object Writes {
  implicit val artifactWrite: Writes[Artifact] = Json.writes[Artifact]
  implicit val testCaseWrite: Writes[TestCase] = Json.writes[TestCase]
  implicit val testCasePackageWrite: Writes[TestCasePackage] = Json.writes[TestCasePackage]
  implicit val buildNodeWrite: Writes[BuildNode] = Json.writes[BuildNode]
  implicit val commitWrite: Writes[Commit] = Json.writes[Commit]
  val buildWrite = Json.writes[Build]
  implicit val mergeResultWrite = Json.writes[MergeResult]
  implicit val buildParametersCategoryWrite = Json.writes[BuildParametersCategory]


implicit val buildActionWrite = ((__ \ "name").write[String] ~
    (__ \ "pullRequestId").writeNullable[Int] ~
    (__ \ "branchId").writeNullable[String] ~
    (__ \ "cycleName").write[String] ~
    (__ \ "buildParametersCategories").write(list(buildParametersCategoryWrite)))(unlift(BuildAction.unapply))

  implicit val entityAssignment = Json.writes[Assignment]
  implicit val entityStateWrite = Json.writes[EntityState]
  implicit val entityWrite = Json.writes[Entity]

  implicit val statusWrites = Json.writes[PullRequestStatus]

  val prWrite = Json.writes[PullRequest]

  implicit val activityEntryWrites = new Writes[ActivityEntry] {
    override def writes(o: ActivityEntry): JsValue = o match {
      case b: Build => buildWrite.writes(b)
      case b: PullRequest => prWrite.writes(b)
      case c: Commit => commitWrite.writes(c)
    }
  }



  implicit val branchWrite = Json.writes[Branch]


  implicit val magicMergeResultWrite = (
    (__ \ "message").write[String] ~
      (__ \ "merged").write[Boolean] ~
      (__ \ "deleted").write[Boolean] ~
      (__ \ "closed").write[Boolean]
    )((m: MagicMergeResult) => (m.description, m.merged, m.deleted, m.closed))
}
