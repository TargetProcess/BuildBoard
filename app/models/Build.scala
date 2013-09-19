package models

import org.joda.time.DateTime

object BuildResult extends Enumeration {
  type BuildResult = Value
  val UNKNOWN, SUCCESS, FAILURE, ABORTED = Value
}


case class Build(pullRequestId: String, result:String, url: String, timestamp: DateTime, number: Int)
