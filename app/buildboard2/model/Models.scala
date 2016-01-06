package buildboard2.model

import models.Build
import org.joda.time.DateTime


case class Account(name: Option[String], toolToken: String, config: AccountConfig)

case class AccountConfig(user: String, token: String)

case class BuildInfo(id: String,
                     name: String,
                     timestamp: DateTime,
                     number: Int,
                     url: String,
                     config: Map[String, String],
                     initiator: Option[String],
                     branch: Option[String],
                     pullRequestId: Option[Int],
                     status: Option[String],
                     commit: Option[String]) {
  def this(build: Build) {
    this(build.number.toString,
      name = build.name,
      timestamp = build.timestamp,
      build.number,
      build.node.map(_.statusUrl).getOrElse(""),
      Map.empty,
      build.initiator,
      if (build.pullRequestId.isEmpty) Some(build.branch) else None,
      build.pullRequestId,
      build.status,
      build.ref)
  }
}






