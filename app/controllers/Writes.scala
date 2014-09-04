package controllers

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
  implicit val buildInfoWrite = Json.writes[BuildInfo]
  implicit val buildWrite = Json.writes[Build]
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

  implicit val prWrite = Json.writes[PullRequest]

  val activityEntryWrites = new Writes[ActivityEntry] {
    override def writes(o: ActivityEntry): JsValue = o match {
      case b: Build => buildWrite.writes(b)
      case b: PullRequest => prWrite.writes(b)
      case b: BuildInfo => buildInfoWrite.writes(b)
      case c: Commit => commitWrite.writes(c)
    }
  }

  implicit val branchWrite =
    (
      (__ \ "name").write[String] ~
        (__ \ "url").write[String] ~
        (__ \ "pullRequest").writeNullable[PullRequest] ~
        (__ \ "entity").writeNullable[Entity] ~
        (__ \ "lastBuild").writeNullable[BuildInfo] ~
        (__ \ "activity").write(list(activityEntryWrites)) ~
        (__ \ "buildActions").write(list[BuildAction])
      )(unlift(BranchInfo.serialize))


  implicit val magicMergeResultWrite = (
    (__ \ "message").write[String] ~
      (__ \ "merged").write[Boolean] ~
      (__ \ "deleted").write[Boolean] ~
      (__ \ "closed").write[Boolean]
    )((m: MagicMergeResult) => (m.description, m.merged, m.deleted, m.closed))
}
