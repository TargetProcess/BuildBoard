package components

import models.User
import models.branches.Branch

trait BranchServiceComponent {
  val branchService: BranchService

  trait BranchService {
    def getBranches: List[Branch]
  }

}


