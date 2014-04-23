package models.services

import models.AuthInfo
import scala.util.matching.Regex
import src.Utils.watch
import components._
import models.Branch
import scala.Some


trait BranchServiceComponentImpl extends BranchServiceComponent {
  this: BranchServiceComponentImpl
    with AuthInfoProviderComponent
    with GithubServiceComponent
    with TargetprocessComponent
  =>

  val branchService:BranchService = new BranchServiceImpl(authInfo)

  class BranchServiceImpl(user: AuthInfo) extends BranchService {
    val EntityBranchPattern = new Regex("^(?i)feature/(us|bug|f)(\\d+).*")
    val FeatureBranchPattern = new Regex("^(?i)feature/(\\w+)")


    def getBranches: List[Branch] = watch("Get branches") {
      val pullRequests = watch("Get pull requests") {
        githubService.getPullRequests
      }
      val branches = watch("Get branches") {
        githubService.getBranches
      }
      val entityIds = branches
        .map(br => br.name).flatMap {
        case EntityBranchPattern(_, id) => Some(id.toInt)
        case _ => None
      }
        .toList

      val entities = watch("Get assignables") {
        entityRepository.getAssignables(entityIds)
      }
        .map(e => (e.id, e))
        .toMap

      watch("Parse branch info") {
        branches.map(branch => branch.copy(pullRequest = pullRequests
          .find(p => p.name == branch.name)
          .map(pr => pr.copy(status = githubService.getPullRequestStatus(pr.prId))))
        )
          .map(branch => branch.copy(entity = branch.name match {
          case EntityBranchPattern(_, id) => entities.get(id.toInt)
          case _ => None
        }))
      }
    }
  }

}