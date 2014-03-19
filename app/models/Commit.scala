package models

import org.joda.time.DateTime

case class Commit(sha1: String, message: String, committerEmail: String, timestamp: DateTime, activityType:String = "commit") extends ActivityEntry{

}



