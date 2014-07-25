package components

import models.{BranchInfo, Branch}

trait BranchRepositoryComponent {
  val branchRepository: BranchRepository

  trait BranchRepository {
    def getBranch(id: String): Option[Branch]
    def getBranchEntity(i: Int): Option[Branch]

    def getBranchByPullRequest(id: Int): Option[Branch]

    def getBranchInfos: List[BranchInfo]

    def getBranches:List[Branch]

    def remove(branch:Branch)

    def update(branch:Branch)
  }

}
