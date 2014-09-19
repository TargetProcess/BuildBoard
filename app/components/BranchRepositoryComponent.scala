package components

import models.branches.Branch

trait BranchRepositoryComponent {
  val branchRepository: BranchRepository

  trait BranchRepository {
    def getBranchesWithLastBuild: List[Branch]


    def getBranch(id: String): Option[Branch]
    def getBranchEntity(i: Int): Option[Branch]

    def getBranchByPullRequest(id: Int): Option[Branch]

    def getBranches:List[Branch]

    def remove(branch:Branch)

    def update(branch:Branch)
  }

}
