package buildboard2

import buildboard2.model.{AccountConfig, Account}
import play.api.libs.json._

object Reads2 {
  implicit val accountConfigReads: Reads[AccountConfig] = Json.reads[AccountConfig]
  implicit val accountReads: Reads[Account] = Json.reads[Account]
}
