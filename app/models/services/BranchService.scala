package models.services

import models.{ActivityEntry, User}
import models.github.GithubRepository
import models.tp.EntityRepo
import scala.util.matching.Regex
import models.jenkins.JenkinsRepository

class BranchService(implicit user: User) {
  val EntityBranchPattern = new Regex("^(?i)feature/(us|bug|f)(\\d+).*")
  val FeatureBranchPattern = new Regex("^(?i)feature/(\\w+)")

  val githubRepository = new GithubRepository
  val entityRepository = new EntityRepo(user.token)
  val jenkinsRepository = new JenkinsRepository

  def getBranches = {
    val pullRequests = githubRepository.getPullRequests
    val branches = githubRepository.getBranches
    val branchNames = branches
      .map(br => br.name)

    val entityIds = branchNames.flatMap {
      case EntityBranchPattern(_, id) => Some(id.toInt)
      case _ => None
    }
      .toList

    val entities = entityRepository.getAssignables(entityIds)
      .map(e => (e.id, e))
      .toMap

    branches.map(branch => {
      val pullRequest = pullRequests
        .find(p => p.name == branch.name)
        .map(pr => pr.copy(status = githubRepository.getPullRequestStatus(pr.prId)))
      val entity = branch.name match {
        case EntityBranchPattern(_, id) => entities.get(id.toInt)
        case _ => None
      }
      val builds = jenkinsRepository.getBuildInfos(branch)
      val lastBuild = builds.headOption
      val commits = githubRepository.getCommits(builds.flatMap(_.commits))

      val activity: List[ActivityEntry] = builds ++ (if (pullRequest.isDefined) List(pullRequest.get) else Nil) ++ commits

      branch.copy(entity = entity, pullRequest = pullRequest, lastBuild = lastBuild, activity = activity)
    })
  }
}
