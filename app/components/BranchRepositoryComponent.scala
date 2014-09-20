package components

import models.Branch

trait BranchRepositoryComponent {
  val branchRepository: BranchRepository

  trait BranchRepository {
    def getBranch(name: String): Option[Branch]
    def getBranchByEntity(id: Int): Option[Branch]
    def getBranchByPullRequest(id: Int): Option[Branch]

    def getBranches:Iterator[Branch]

    def remove(branch:Branch)

    def update(branch:Branch)
  }

}
