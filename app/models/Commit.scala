package models

import org.joda.time.DateTime

case class Commit(sha1: String, message: String, committerEmail: String, timestamp: DateTime) extends ActivityEntry{
  override val activityType = "commit"
}



