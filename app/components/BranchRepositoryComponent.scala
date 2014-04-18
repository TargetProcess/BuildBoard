package components

import models.{BranchInfo, Branch}

trait BranchRepositoryComponent {
  def branchRepository: BranchRepository

  trait BranchRepository {
    def getBranch(id: String): Option[Branch]

    def getBranches: List[BranchInfo]
  }

}
