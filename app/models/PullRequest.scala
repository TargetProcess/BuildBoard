package models

import com.github.nscala_time.time.Imports._

case class PullRequest(name: String, prId: Int, url: String, timestamp: DateTime, status: PullRequestStatus, activityType: String = "pullRequest") extends ActivityEntry

case class PullRequestStatus(isMergeable: Boolean, isMerged: Boolean, isLgtm: Boolean)
