package controllers

import com.github.nscala_time.time.Imports._
import controllers.Reads._
import controllers.Writes._
import models.BuildStatus.{InProgress, Unknown}
import models._
import models.buildActions._
import models.cycles.{CustomCycle, Cycle}
import play.Play
import play.api.libs.json._

import scala.util.{Failure, Success, Try}
import scalaj.http.HttpException

case class ForceBuildParameters(pullRequestId: Option[Int],
                                branchId: Option[String],
                                cycleName: String,
                                parameters: List[BuildParametersCategory],
                                buildNumber: Option[Int]
                                 )

object Jenkins extends Application {

  def forceBuild() = IsAuthorizedComponent {
    component =>
      request =>
        request.body.asJson.map { json =>

          val params = json.as[ForceBuildParameters]

          val cycle: Cycle = if (params.parameters.isEmpty) BuildAction.find(params.cycleName) else CustomCycle(params.parameters)

          val maybeAction: Option[BuildAction] =
            (params.buildNumber, params.pullRequestId, params.branchId) match {
              case (Some(number), _, Some(branch)) => Some(ReuseArtifactsBuildAction(branch, number, cycle))
              case (None, Some(prId), None) => Some(PullRequestBuildAction(prId, cycle))
              case (None, None, Some(brId)) => Some(BranchBuildAction(brId, cycle))
              case _ => None
            }

          maybeAction match {
            case Some(buildAction) =>
              val forceBuildResult: Try[Any] = component.jenkinsService.forceBuild(buildAction)
              forceBuildResult match {
                case Success(_) => Ok(Json.toJson(Build(-1, params.branchId.getOrElse("this"), Some("In progress"), DateTime.now,
                  name = "", node = Some(BuildNode("this", "this", Some("In progress"), "#", List(), DateTime.now, None)))))
                case Failure(e: HttpException) => BadRequest(e.toString)
                case Failure(e) => throw e
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
        val branch: Option[Branch] = component.branchRepository.getBranch(branchName)


        val list = branch.flatMap(b => {
          number match {
            case Some(id) => component.buildRepository.getBuild(b, id)
              .map(build => List(ReuseArtifactsBuildAction(build.name, build.number)))
            case None => Some(b.buildActions)
          }
        })

        Ok(Json.toJson(list.getOrElse(Nil)))
  }


  def lastBuilds(branch: String, count: Int) = IsAuthorizedComponent {
    component =>
      request =>
        Ok(Json.toJson(component.buildRepository.getLastBuilds(branch, count)))
  }


  def buildStatus(id: Int) = IsAuthorizedComponent {
        component =>
          request => {
        val branch = component.branchRepository.getBranchByEntity(id)
        val status = branch
          .flatMap(b => component.buildRepository.getLastBuilds(b.name, 1).headOption)
          .map(_.buildStatus)
          .getOrElse(Unknown)

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
}
