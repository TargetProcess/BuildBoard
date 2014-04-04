package components

import models.Branch

trait BranchServiceComponent {
  def branchService : BranchService

  trait BranchService {
    def getBranches: List[Branch]
  }
}
