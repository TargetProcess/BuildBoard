package buildboard2.model

import models.{BuildNode, Build}
import org.joda.time.DateTime


case class Account(name: Option[String], toolToken: String, config: AccountConfig)

case class AccountConfig(user: String, token: String)

case class Build2(id: String,
                  name: String,
                  timestamp: DateTime,
                  number: Int,
                  url: String,
                  config: Map[String, String],
                  initiator: Option[String],
                  branch: Option[String],
                  pullRequest: Option[Int],
                  status: Option[String],
                  commit: Option[String])

object Build2 {
  def create(build: Build): Build2 = {
    Build2(build.number.toString,
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

case class Job2(id: String,
                name: String,
                url: String,
                number: Int,
                build: String,
                timestamp: DateTime,
                parent: Option[String],
                config: Map[String, String],
                status: Option[String])

object Job2 {
  def create(build: Build, buildNode: BuildNode, parentNode: Option[BuildNode]) = {
    Job2(buildNode.id,
      if (buildNode.runName.isEmpty) buildNode.name else s"${buildNode.runName} - ${buildNode.name}",
      buildNode.statusUrl,
      buildNode.number,
      build.number.toString,
      buildNode.timestamp,
      parentNode.map(_.id),
      Map.empty,
      buildNode.status)
  }
}






