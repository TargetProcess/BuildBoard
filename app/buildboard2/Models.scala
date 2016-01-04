package buildboard2

import org.joda.time.DateTime


case class Account(name: Option[String], toolToken: String, config: AccountConfig)

case class AccountConfig(user: String, token: String)

case class BuildInfo(id: String,
                     timestamp: DateTime,
                     number: Int,
                     url: String,
                     config: Map[String, String],
                     initiator: Option[String],
                     branch: Option[String],
                     pullRequestId: Option[Int],
                     status: Option[String],
                     commit: Option[String])






