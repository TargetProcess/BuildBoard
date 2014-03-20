package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import com.github.nscala_time.time.Imports._
import Writes._
import models.jenkins.JenkinsRepository
import scala.util.{Failure, Success}
import scalaj.http.HttpException

object Jenkins extends Controller with Secured {
  val jenkinsRepo = new JenkinsRepository

  def forceBuild(pullRequestId: Option[Int], branchId: Option[String], cycleName: String) = IsAuthorized {
    user =>
      request =>

        val maybeAction: Option[BuildAction] = (pullRequestId, branchId) match {
          case (Some(prId), None) => Some(PullRequestBuildAction(prId, BuildAction.find(cycleName)))
          case (None, Some(brId)) => Some(BranchBuildAction(brId, BuildAction.find(cycleName)))
          case _ => None
        }

        maybeAction match {
          case Some(buildAction) =>
            jenkinsRepo.forceBuild(buildAction) match {
              case Success(_) => Ok(Json.toJson(Build(-1, branchId.getOrElse("this"), DateTime.now, BuildNode("this", "this", Some("In progress"), "#", List(), DateTime.now))))
              case Failure(e: HttpException) => BadRequest(e.toString)
              case Failure(e) => InternalServerError("Something going wrong " + e.toString)
            }
          case None => BadRequest("There is no pullRequestId or branchId")
        }
  }

  def toggleBuild(branchId: String, buildNumber: Int, toggled: Boolean) = IsAuthorized {
    user =>
      val branch = new BranchRepository().getBranch(branchId)
      val build = branch.map(new BuildRepository().toggleBuild(_, buildNumber, toggled))
      request => Ok(Json.toJson(build))
  }

  def build(branch: String, number: Int) = IsAuthorized {
    user =>
      val branchEntity = new BranchRepository().getBranch(branch)
      val build = branchEntity.map(new BuildRepository().getBuild(_, number))
      request => Ok(Json.toJson(build))
  }

  def run(branch: String, build: Int, part: String, run: String) = IsAuthorized {
    user =>
      val branchEntity = new BranchRepository().getBranch(branch)
      val runEntity = branchEntity.map(jenkinsRepo.getTestRun(_, build, part, run))
      request => Ok(Json.toJson(runEntity))
  }

  def testCase(branch: String, build: Int, part: String, run: String, test: String) = IsAuthorized {
    user =>
      val branchEntity = new BranchRepository().getBranch(branch)
      val buildNode = branchEntity.map(jenkinsRepo.getTestRun(_, build, part, run)).flatten
      val testCase = buildNode.map(n => n.getTestCase(test)).flatten
      request => Ok(Json.toJson(testCase))
  }

  def artifact(file: String) = IsAuthorized {
    user =>
      request => Ok.sendFile(content = jenkinsRepo.getArtifact(file))
  }
}
