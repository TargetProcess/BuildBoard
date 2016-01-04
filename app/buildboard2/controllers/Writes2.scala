package buildboard2.controllers

import buildboard2.{Account, AccountConfig, BuildInfo}
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json._


object Writes2 {
  implicit val accountConfigWrites: Writes[AccountConfig] = Json.writes[AccountConfig]
  implicit val accountWrites: Writes[Account] = (
    (__ \ "name").writeNullable[String] ~
      (__ \ "toolToken").write[String] ~
      (__ \ "config").write[AccountConfig]
    ) ((a: Account) => Account.unapply(a).get)
  implicit val buildInfoWrites: Writes[BuildInfo] = Json.writes[BuildInfo]

}
