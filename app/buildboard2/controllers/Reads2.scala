package buildboard2.controllers

import buildboard2.{Account, AccountConfig}
import play.api.libs.json._

object Reads2 {
  implicit val accountConfigReads: Reads[AccountConfig] = Json.reads[AccountConfig]
  implicit val accountReads: Reads[Account] = Json.reads[Account]
}
