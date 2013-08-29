package models

import org.joda.time.DateTime

object BuildResult extends Enumeration {
  type BuildResult = Value
  val UNKNOWN, SUCCESS, FAILURE, ABORTED = Value
}

trait Build {
  val timestamp: DateTime
  val number: Int
  val url: String
  val result: BuildResult.Value
}

case class PullRequestBuild(pullRequestId: String, result: BuildResult.Value, url: String, timestamp: DateTime, number: Int) extends Build