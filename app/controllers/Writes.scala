package controllers

import models.buildActions.{BuildAction, BuildParametersCategory}
import models.magicMerge.MagicMergeResult
import models.{PullRequestStatus, _}
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json._

object Writes {
  implicit val artifactWrite: Writes[Artifact] = Json.writes[Artifact]
  implicit val testCaseWrite: Writes[TestCase] = Json.writes[TestCase]
  implicit val testCasePackageWrite: Writes[TestCasePackage] = Json.writes[TestCasePackage]
  implicit var buildNodeWrite: Writes[BuildNode] = null
  buildNodeWrite = (
    (__ \ "name").write[String] ~
      (__ \ "runName").write[String] ~
      (__ \ "status").writeNullable[String] ~
      (__ \ "statusUrl").write[String] ~
      (__ \ "artifacts").write(list(artifactWrite)) ~
      (__ \ "timestamp").write[DateTime] ~
      (__ \ "rerun").writeNullable[Boolean] ~
      (__ \ "rerun").writeNullable[Boolean] ~
      (__ \ "children").lazyWrite(list(buildNodeWrite)) ~
      (__ \ "testResults").write(list(testCasePackageWrite))
    )((node: BuildNode) => BuildNode.unapply(node.copy(status = Some(node.buildStatus.name.toUpperCase))).get)

  implicit val commitWrite: Writes[Commit] = Json.writes[Commit]

  val buildWrite: Writes[Build] =
    (
      (__ \ "number").write[Int] ~
        (__ \ "branch").write[String] ~
        (__ \ "status").writeNullable[String] ~
        (__ \ "timestamp").write[DateTime] ~
        (__ \ "toggled").write[Boolean] ~
        (__ \ "commits").write(list(commitWrite)) ~
        (__ \ "ref").writeNullable[String] ~
        (__ \ "initiator").writeNullable[String] ~
        (__ \ "description").writeNullable[String] ~
        (__ \ "pullRequestId").writeNullable[Int] ~
        (__ \ "name").write[String] ~
        (__ \ "activityType").write[String] ~
        (__ \ "node").writeNullable[BuildNode]

      )((b: Build) => Build.unapply(b.copy(status = Some(b.buildStatus.name.toUpperCase))).get)


  implicit val mergeResultWrite = Json.writes[MergeResult]
  implicit val buildParametersCategoryWrite = Json.writes[BuildParametersCategory]


  implicit val buildActionWrite = ((__ \ "name").write[String] ~
    (__ \ "pullRequestId").writeNullable[Int] ~
    (__ \ "branchId").writeNullable[String] ~
    (__ \ "cycleName").write[String] ~
    (__ \ "action").write[String] ~
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


  implicit val magicMergeResultWrite: OWrites[MagicMergeResult] = (
    (__ \ "message").write[String] ~
      (__ \ "merged").write[Boolean] ~
      (__ \ "deleted").write[Boolean] ~
      (__ \ "closed").write[Boolean]
    )((m: MagicMergeResult) => (m.description, m.merged, m.deleted, m.closed))
}
