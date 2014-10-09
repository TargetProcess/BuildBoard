package models.magicMerge

import models.PullRequest

case class MagicMergeResult(pullRequest: PullRequest, merged: Boolean, deleted: Boolean, closed: Boolean) {
  val description: String =
    s"""PR#${pullRequest.prId} ${is(merged, "merged")},
       | branch ${is(deleted, "deleted")},
       | entity ${is(closed, "closed")}
     """.stripMargin

  private def is(flag: Boolean, value: String) = (if (flag) "is " else "is not ") + value
}
