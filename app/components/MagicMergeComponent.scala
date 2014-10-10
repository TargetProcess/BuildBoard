package components

import models.User
import models.magicMerge.MagicMergeResult

import scala.util.Try

trait MagicMergeComponent {
  def magicMergeService: MagicMergeService

  trait MagicMergeService {
    def merge(branchName: String, user:User): Try[MagicMergeResult]
  }

}

