package controllers

import play.api.mvc._
import models._
import models.github.GitHubRepository
import play.api.libs.json._
import Writes._
import models.jenkins.JenkinsRepository

object Application extends Controller with Secured {

  def index = {
    IsAuthorized {
      implicit user =>
        implicit request => Ok(views.html.index(user))
    }
  }

  def branches = IsAuthorized {
    implicit user =>
      implicit request =>
        val branches = new GitHubRepository().getBranches

        Ok(Json.toJson(branches))
  }

  def branch(id:String) = IsAuthorized {
      implicit user =>
        implicit request =>
          val branch: Branch = new GitHubRepository().getBranch(id)

          Ok(Json.toJson(branch))
  }

  def builds(branch: String) = IsAuthorized {
    implicit user =>
      val branchEntity = new GitHubRepository().getBranch(branch)
      request => Ok(Json.toJson(JenkinsRepository.getBuilds(branchEntity)))
  }

  def lastBuildInfo(branch: String) = IsAuthorized {
    implicit user =>
      val branchEntity = new GitHubRepository().getBranch(branch)
      request => Ok(Json.toJson(JenkinsRepository.getLastBuild(branchEntity)))
  }

  def lastBuildInfos = IsAuthorized {
    implicit user =>
      val branches = new GitHubRepository().getBranches
      request => Ok(Json.toJson(JenkinsRepository.getLastBuildsByBranch(branches)))
  }

  def build(branch: String, number: Int) = IsAuthorized {
    implicit user =>
      val branchEntity = new GitHubRepository().getBranch(branch)
      request => Ok(Json.toJson(JenkinsRepository.getBuild(branchEntity, number)))
  }
}
