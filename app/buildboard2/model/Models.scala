package buildboard2.model

import models.{Build, BuildNode}
import org.joda.time.DateTime


case class Account(name: Option[String], toolToken: String, config: AccountConfig, resources: List[String] = Nil)

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
    Build2(getId(build),
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
  def getId(build: Build): String = build.name
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
    Job2(getId(build, buildNode),
      getName(buildNode),
      buildNode.statusUrl,
      buildNode.number,
      Build2.getId(build),
      buildNode.timestamp,
      parentNode.map(_.id),
      Map.empty,
      buildNode.status)
  }
  def getId(build: Build, buildNode: BuildNode) = s"${Build2.getId(build)} - ${getName(buildNode)}"
  def getName(buildNode: BuildNode) = if (buildNode.runName.isEmpty) buildNode.name else s"${buildNode.runName} - ${buildNode.name}"
}

case class Artifact2(id: String, name: String, url: String, job: Option[String] = None, build: Option[String] = None)






