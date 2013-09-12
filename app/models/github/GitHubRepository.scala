package models.github

import models.tp.EntityRepo
import models._
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service._
import org.eclipse.egit.github.core.{RepositoryBranch, RepositoryId, PullRequest=>GhPullRequest}
import scala.collection.JavaConverters._
import models.jenkins.JenkinsRepository

class GitHubRepository(implicit user: User) {
  val github = new GitHubClient().setOAuth2Token(user.githubToken)
  val repositoryService = new RepositoryService(github)
  val prService = new PullRequestService(github)

  val EntityBranchPattern = "^(?i)feature/(us|bug|f)(\\d+).*".r
  val FeatureBranchPattern = "^(?i)feature/(\\w+)".r
  val repo = new RepositoryId("TargetProcess", "TP")


  def getBranches: List[Branch] = {
    if (user.githubToken == null) {
      Nil
    }
    else {
      val ghBranches:List[RepositoryBranch] = repositoryService.getBranches(repo).asScala.toList
      val ghPullRequests:Map[String,GhPullRequest] = prService.getPullRequests(repo, "OPEN").asScala.map(pr => (pr.getHead.getRef, pr)).toMap

      val branchNames = ghBranches
        .map(br => br.getName)

      val entityIds = branchNames
        .flatMap {
        case EntityBranchPattern(_, id) => Some(id.toInt)
        case _ => None
      }
        .toList

      val entities = new EntityRepo(user).getAssignables(entityIds)
        .map(e => (e.id, e))
        .toMap

      val builds = JenkinsRepository.getBuilds

      ghBranches.map(githubBranch => {
        val name = githubBranch.getName

        val pullRequest = ghPullRequests.get(name)
          .map(pr => PullRequest(pr))


        val entity = name match {
          case EntityBranchPattern(_, id) => entities.get(id.toInt)
          case _ => None
        }

        val pullRequestId = pullRequest.map(x => x.id)

        val branchBuilds = builds.filter(b => b match {
          case PullRequestBuild(prId: String, _, _, _, _) => prId == name || (pullRequestId.isDefined && prId == pullRequestId)
          case _ => false
        })

        Branch(name, pullRequest, entity, branchBuilds)

      }).toList
    }
  }

  def getPullRequestStatus(id: Int) = {
    var pr = prService.getPullRequest(repo, id)
    PullRequestStatus(pr.isMergeable, pr.isMerged)
  }

  def getBranch(name: String) = {
    val ghBranches = repositoryService.getBranches(repo).asScala.find(p => p.getName == name)
    ghBranches
  }


}
