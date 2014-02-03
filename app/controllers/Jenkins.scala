package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import com.github.nscala_time.time.Imports._
import Writes._
import models.jenkins.{JenkinsRepository}
import scala.util.{Failure, Success}
import scalaj.http.HttpException

object Jenkins extends Controller with Secured {
  val jenkinsRepo = JenkinsRepository

  def forceBuild(pullRequestId: Option[Int], branchId: Option[String], fullCycle: Boolean) = IsAuthorized {
    implicit user =>
      request =>
        val maybeAction: Option[BuildAction] = (pullRequestId, branchId) match {
          case (Some(prId), None) => Some(PullRequestBuildAction(prId, fullCycle))
          case (None, Some(brId)) => Some(BranchBuildAction(brId, fullCycle))
          case _ => None
        }
        maybeAction match {
          case Some(buildAction) =>
            val buildResult = jenkinsRepo.forceBuild(buildAction)
            buildResult match {
              case Success(_) => Ok(Json.toJson(
                Build(-1, "this", Some("In progress"), "#", DateTime.now, BuildNode("this", "this", Some("In progress"), "#", None, DateTime.now))
              ))
              case Failure(e: HttpException) => BadRequest(e.message)
              case Failure(e) => InternalServerError("Something going wrong " + e.toString)
            }
          case None => BadRequest("There is no pullRequestId or branchId")
        }
  }

  def toggleBuild(branchId: String, buildNumber: Int) = IsAuthorized {
    implicit user =>
      val branch = new BranchesRepository().getBranch(branchId)
      val build = jenkinsRepo.toggleBuild(branch, buildNumber)
      request => Ok(Json.toJson(build))
  }

  def builds(branch: String) = IsAuthorized {
    implicit user =>
      val branchEntity = new BranchesRepository().getBranch(branch)
      request => Ok(Json.toJson(jenkinsRepo.getBuilds(branchEntity).toList))
  }

  def lastBuildInfo(branch: String) = IsAuthorized {
    implicit user =>
      val branchEntity = new BranchesRepository().getBranch(branch)
      request => Ok(Json.toJson(jenkinsRepo.getLastBuild(branchEntity)))
  }

  def lastBuildInfos = IsAuthorized {
    implicit user =>
      val branches = new BranchesRepository().getBranches
      request => Ok(Json.toJson(jenkinsRepo.getLastBuildsByBranch(branches)))
  }

  def build(branch: String, number: Int) = IsAuthorized {
    implicit user =>
      val branchEntity = new BranchesRepository().getBranch(branch)
      request => Ok(Json.toJson(jenkinsRepo.getBuild(branchEntity, number)))
  }

  def artifact(file: String) = IsAuthorized {
    implicit user =>
      request => Ok(Json.toJson(jenkinsRepo.getArtifact(file)))
  }
}
