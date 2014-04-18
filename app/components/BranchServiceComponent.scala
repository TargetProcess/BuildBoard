package components

import models.Branch

trait BranchServiceComponent {
  val branchService: BranchService

  trait BranchService {
    def getBranches: List[Branch]
  }

}
