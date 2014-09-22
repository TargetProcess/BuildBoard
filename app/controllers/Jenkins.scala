package controllers

import com.github.nscala_time.time.Imports._
import controllers.Reads._
import controllers.Writes._
import models._
import play.api.libs.json._

import scala.util.{Try, Failure, Success}
import scalaj.http.HttpException

case class ForceBuildParameters(pullRequestId: Option[Int], branchId: Option[String], cycleName: String, parameters: List[BuildParametersCategory]) {
}

object Jenkins extends Application {

  def forceBuild() = IsAuthorizedComponent {
    component =>
      request =>

        request.body.asJson.map { json =>

          val params = json.as[ForceBuildParameters]
          val cycle = if (params.parameters.isEmpty) BuildAction.find(params.cycleName) else CustomCycle(params.parameters)

          val maybeAction: Option[BuildAction] = (params.pullRequestId, params.branchId) match {
            case (Some(prId), None) => Some(PullRequestBuildAction(prId, cycle))
            case (None, Some(brId)) => Some(BranchBuildAction(brId, cycle))
            case _ => None
          }

          maybeAction match {
            case Some(buildAction) =>
              val forceBuildResult: Try[String] = component.jenkinsService.forceBuild(buildAction)
              forceBuildResult match {
                case Success(_) => Ok(Json.toJson(Build(-1, params.branchId.getOrElse("this"), Some("In progress"), DateTime.now,
                  name = "", node = Some(BuildNode("this", "this", Some("In progress"), "#", List(), DateTime.now)))))
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

  def buildActions(branchName: String, number: Option[Int]) = IsAuthorizedComponent {
    component =>
      request =>
      val branch = component.branchRepository.getBranch(branchName)

      Ok(Json.toJson(branch.map(_.buildActions).getOrElse(Nil)))
  }
}
