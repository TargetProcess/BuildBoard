package controllers

import models.BuildStatus.Unknown
import models._
import play.Play
import play.api.libs.json._
import com.github.nscala_time.time.Imports._
import Writes._
import scala.util.{Failure, Success}
import scalaj.http.HttpException

object Jenkins extends Application {

  def forceBuild(pullRequestId: Option[Int], branchId: Option[String], cycleName: String) = IsAuthorizedComponent {
    component =>
      request =>


        val maybeAction: Option[BuildAction] = (pullRequestId, branchId) match {
          case (Some(prId), None) => Some(PullRequestBuildAction(prId, BuildAction.find(cycleName)))
          case (None, Some(brId)) => Some(BranchBuildAction(brId, BuildAction.find(cycleName)))
          case _ => None
        }

        maybeAction match {
          case Some(buildAction) =>
            component.jenkinsService.forceBuild(buildAction) match {
              case Success(_) => Ok(Json.toJson(Build(-1, branchId.getOrElse("this"), Some("In progress"), DateTime.now,
                name = "", node = Some(BuildNode("this", "this", Some("In progress"), "#", List(), DateTime.now)))))
              case Failure(e: HttpException) => BadRequest(e.toString)
              case Failure(e) => InternalServerError("Something going wrong " + e.toString)
            }
          case None => BadRequest("There is no pullRequestId or branchId")
        }
  }

  def toggleBuild(branchId: String, buildNumber: Int, toggled: Boolean) = IsAuthorizedComponent {
    repository =>
      val optionBranch = repository.branchRepository.getBranch(branchId)
      val optionBuild = optionBranch.flatMap(repository.buildRepository.toggleBuild(_, buildNumber, toggled))

      optionBuild.foreach(repository.notificationService.notifyToggle(optionBranch.get, _))


      request => Ok(Json.toJson(optionBuild))
  }

  def build(branch: String, number: Int) = IsAuthorizedComponent {
    registry =>
      val branchEntity = registry.branchRepository.getBranch(branch)
      val build = branchEntity.map(registry.buildRepository.getBuild(_, number))
      request => Ok(Json.toJson(build))
  }

  def run(branch: String, build: Int, part: String, run: String) = IsAuthorizedComponent {
    component =>

      val branchEntity = component.branchRepository.getBranch(branch)
      val runEntity = branchEntity.map(component.jenkinsService.getTestRun(_, build, part, run))
      request => Ok(Json.toJson(runEntity))
  }

  def testCase(branch: String, build: Int, part: String, run: String, test: String) = IsAuthorizedComponent {
    component =>

      val branchEntity = component.branchRepository.getBranch(branch)
      val buildNode: Option[BuildNode] = branchEntity.map(component.jenkinsService.getTestRun(_, build, part, run)).flatten
      val testCase = buildNode.map(n => n.getTestCase(test)).flatten
      request => Ok(Json.toJson(testCase))
  }

  def artifact(file: String) = IsAuthorizedComponent {
    component =>
      request => Ok.sendFile(content = component.jenkinsService.getArtifact(file))
  }


  def buildStatus(id: Int) = IsAuthorizedComponent {
    component =>
      request => {
        val branch = component.branchRepository.getBranchEntity(id)
        val status = (for (b<-branch    ;
          lastBuild <- component.buildRepository.getLastBuild(b)
        ) yield lastBuild.buildStatus).getOrElse(Unknown)

       val fileName = status.success match{
         case None => "unknown"
         case Some(true)=> "ok"
         case Some(false)=> "fail"
       }

        val file = Play.application.getFile(s"images/build/$fileName.png")

        Ok.sendFile(file, inline = true)
      }
  }
}
