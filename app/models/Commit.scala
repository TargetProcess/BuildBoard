package models

import org.joda.time.DateTime

case class Commit(sha1: String, message: String, authorName: String, authorEmail: String, timestamp: DateTime, activityType: String = "commit") extends ActivityEntry {}



