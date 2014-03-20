package models.services

import models.{AuthInfo, ActivityEntry, User}
import models.github.GithubRepository
import models.tp.EntityRepo
import scala.util.matching.Regex
import models.jenkins.JenkinsRepository
import src.Utils.watch

class BranchService(user: AuthInfo) {
  val EntityBranchPattern = new Regex("^(?i)feature/(us|bug|f)(\\d+).*")
  val FeatureBranchPattern = new Regex("^(?i)feature/(\\w+)")

  val githubRepository = new GithubRepository(user)
  val entityRepository = new EntityRepo(user.token)
  val jenkinsRepository = new JenkinsRepository

  def getBranches = watch("Get branches") {
    val pullRequests = watch("Get pull requests") {
      githubRepository.getPullRequests
    }
    val branches = watch("Get branches") {
      githubRepository.getBranches
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
          .map(pr => pr.copy(status = githubRepository.getPullRequestStatus(pr.prId))))
        )
        .map(branch => branch.copy(entity = branch.name match {
          case EntityBranchPattern(_, id) => entities.get(id.toInt)
          case _ => None
        }))
        .map(branch => {
          val pullRequest = branch.pullRequest
          val builds = jenkinsRepository.getBuildInfos(branch)
          val lastBuild = builds.headOption
          val commits = builds.flatMap(b => b.commits)

          val activity: List[ActivityEntry] = builds ++ (if (pullRequest.isDefined) List(pullRequest.get) else Nil) ++ commits

          branch.copy(lastBuild = lastBuild, activity = activity)
        })
    }
  }
}