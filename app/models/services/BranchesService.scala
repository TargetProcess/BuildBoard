package models.services

import models.{ActivityEntry, User}
import models.github.RealGithubRepository
import models.tp.EntityRepo
import scala.util.matching.Regex
import models.jenkins.JenkinsRepository

class BranchesService(implicit user: User) {
  val EntityBranchPattern = new Regex("^(?i)feature/(us|bug|f)(\\d+).*")
  val FeatureBranchPattern = new Regex("^(?i)feature/(\\w+)")

  val githubRepository = new RealGithubRepository
  val entityRepository = new EntityRepo(user.token)
  val jenkinsRepository = new JenkinsRepository

  def getBranches = {
    val pullRequests = githubRepository.getPullRequests
    println(s"pull requests ${pullRequests.length}")
    val branches = githubRepository.getBranches
    println(s"branches ${branches.length}")
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

    println(s"entities ${entities.toList.length}")

    val result = branches.map(branch => {
      val pullRequest = pullRequests
        .find(p => p.name == branch.name)
        .map(pr => pr.copy(status = githubRepository.getPullRequestStatus(pr.prId)))

      val entity = branch.name match {
        case EntityBranchPattern(_, id) => entities.get(id.toInt)
        case _ => None
      }
      //todo: get full builds with test results
      val builds = jenkinsRepository.getBuilds(branch)
      val lastBuild = builds.headOption

      val commits = githubRepository.getCommits(builds.flatMap(_.commits))

      val activity: List[ActivityEntry] = builds ++ (if (pullRequest.isDefined) List(pullRequest.get) else Nil) ++ commits

      branch.copy(entity = entity, pullRequest = pullRequest, lastBuild = lastBuild, activity = activity)
    })

    println(s"branches total ${branches.length}")

    result
  }
}
