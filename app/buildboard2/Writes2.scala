package buildboard2

import buildboard2.controllers.PageResult
import buildboard2.model._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json._


object Writes2 {
  implicit val accountConfigWrites: Writes[AccountConfig] = Json.writes[AccountConfig]
  implicit val accountWrites: Writes[Account] = (
    (__ \ "name").writeNullable[String] ~
      (__ \ "toolToken").write[String] ~
      (__ \ "config").write[AccountConfig] ~
      (__ \ "resources").write(list[String])
    ) ((a: Account) => Account.unapply(a).get)
  implicit val build2Writes: Writes[Build2] = Json.writes[Build2]
  implicit val job2Writes: Writes[Job2] = Json.writes[Job2]
  implicit val artifact2Writes: Writes[Artifact2] = Json.writes[Artifact2]

  implicit def write[A](implicit write: Writes[A]): Writes[PageResult[A]] = (
    (__ \ "items").write[List[A]] ~
      (__ \ "next").writeNullable[String]
    ) (pageResult => PageResult.unapply[A](pageResult).get)
}
