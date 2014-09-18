package controllers

import com.github.nscala_time.time.Imports._
import controllers.Reads._
import controllers.Writes._
import models.BuildStatus.{InProgress, Unknown}
import models._
import play.Play
import play.api.libs.json._

import scala.util.{Failure, Success}
import scalaj.http.HttpException

case class ForceBuildParameters(pullRequestId: Option[Int], branchId: Option[String], cycleName: String, buildNumber: Option[Int], parameters: List[BuildParametersCategory]) {
}

object Jenkins extends Application {

  def forceBuild() = IsAuthorizedComponent {
    component =>
      request =>

        request.body.asJson.map { json =>

          val params = json.as[ForceBuildParameters]

          val maybeAction: Option[BuildAction] = (params.pullRequestId, params.branchId, params.buildNumber, params.parameters) match {
            case (Some(prId), None, None, Nil) => Some(PullRequestBuildAction(prId, BuildAction.find(params.cycleName)))
            case (None, Some(brId), None, Nil) => Some(BranchBuildAction(brId, BuildAction.find(params.cycleName)))
            case (None, Some(brId), None, x :: xs) => Some(BranchCustomBuildAction(brId, CustomCycle(x :: xs)))
            case (Some(prId), None, None, x :: xs) => Some(PullRequestCustomBuildAction(prId, CustomCycle(x :: xs)))
            case (None, Some(brId), Some(buildNumber), x :: xs) => Some(BranchWithArtifactsReuseCustomBuildAction(brId, buildNumber, CustomCycle(x :: xs)))
            case _ => None
          }

          maybeAction match {
            case Some(buildAction) =>
              val forceBuildResult = buildAction match {
                case c@BranchWithArtifactsReuseCustomBuildAction(brId, buildNumber, CustomCycle(x :: xs)) => component.jenkinsService.forceReuseArtifactsBuild(c)
                case _ => component.jenkinsService.forceBuild(buildAction)
              }
              forceBuildResult match {
                case Success(_) => Ok(Json.toJson(
                  Build(
                    number = -1,
                    branch = params.branchId.getOrElse("this"), status = Some("In progress"),
                    timestamp = DateTime.now,
                    name = "",
                    node = Some(BuildNode("this", "this", Some("In progress"), "#", List(), DateTime.now))
                  )))
                case Failure(e: HttpException) => BadRequest(e.toString)
                case Failure(e) => InternalServerError("Something going wrong " + e.toString)
              }
            case None => BadRequest("There is no pullRequestId or branchId")
          }
        }.getOrElse {
          BadRequest("Expecting Json data")
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
        val status = (for (b <- branch;
                           lastBuild <- component.buildRepository.getLastBuild(b)
        ) yield lastBuild.buildStatus).getOrElse(Unknown)

        val fileName = status match {
          case InProgress => "inprogress"
          case _ => status.success match {
            case None => "unknown"
            case Some(true) => "ok"
            case Some(false) => "fail"
          }
        }

        val file = Play.application.getFile(s"public/images/build/$fileName.png")

        Ok.sendFile(file, inline = true)
      }
  }


  def buildActions(branch:String, number: Int) = IsAuthorizedComponent(
  component=>
    request =>
      Ok(Json.toJson(component.jenkinsService.getCustomBuildActions(branch, number)))
  )
}
