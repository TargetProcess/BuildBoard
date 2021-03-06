package controllers

import java.nio.file.Paths

import com.github.nscala_time.time.Imports._
import components.CycleBuilderComponent
import controllers.Formats._
import models.BuildStatus.{InProgress, Unknown}
import models._
import models.buildActions._
import models.cycles.Cycle
import play.Play
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scalaj.http.HttpException

case class ForceBuildParameters(pullRequestId: Option[Int],
                                branchId: Option[String],
                                buildNumber: Option[Int],
                                cycleName: String,
                                parameters: List[BuildParametersCategory]
                               )

object Jenkins extends Application {

  implicit val buildParametersCategoryReads = (
    (__ \ "name").read[String] ~
      (__ \ "parts").read[List[String]] ~
      (__ \ "params").read(Reads.optionNoError[Map[String, String]])
    ) ((name, parts, params) => BuildParametersCategory(name, None, parts, params.getOrElse(Map.empty)))
  implicit val buildParameterCategoryReads: play.api.libs.json.Reads[List[BuildParametersCategory]] = play.api.libs.json.Reads.list[BuildParametersCategory]
  implicit val reads = Json.reads[ForceBuildParameters]

  def createBuildAction(forceBuildParameters: ForceBuildParameters, component: CycleBuilderComponent): Option[JenkinsBuildAction] = {

    if (forceBuildParameters.cycleName == "transifex") {
      forceBuildParameters.branchId.map(TransifexBuildAction)
    }
    else {

      val cycle: Cycle = if (forceBuildParameters.parameters.isEmpty)
        component.cycleBuilder.find(forceBuildParameters.cycleName).get
      else
        component.cycleBuilder.customCycle(forceBuildParameters.parameters)

      val maybeAction: Option[JenkinsBuildAction] =
        (forceBuildParameters.buildNumber, forceBuildParameters.pullRequestId, forceBuildParameters.branchId) match {
          case (Some(number), _, Some(branch)) => Some(ReuseArtifactsBuildAction(branch, number, cycle))
          case (None, Some(prId), None) => Some(PullRequestBuildAction(prId, cycle))
          case (None, None, Some(brId)) => Some(BranchBuildAction(brId, cycle))

          case _ => None
        }
      maybeAction
    }
  }


  def forceBuild() = AuthorizedComponent {
    component =>
      request =>
        request.body.asJson.map { json =>

          val params = json.as[ForceBuildParameters]

          createBuildAction(params, component)
            .map(buildAction => {
              val forceBuildResult: Try[Any] = component.forceBuildService.forceBuild(buildAction)
              forceBuildResult match {
                case Success(_) => Ok(Json.toJson(
                  Build(
                    number = -1,
                    branch = params.branchId.getOrElse("this"),
                    status = Some("In progress"),
                    timestamp = DateTime.now,
                    timestampEnd = None,
                    name = "",
                    node = Some(BuildNode("-1", "this", "this", -1, Some("In progress"), "#", List(), DateTime.now, None, None))
                    )))
                case Failure(e: HttpException) => BadRequest(e.toString)
                case Failure(e) => throw e
              }
            }).getOrElse(BadRequest("There is no pullRequestId or branchId"))

        }.getOrElse {
          BadRequest("Expecting Json data")
        }
  }


  def toggleBuild(branchId: String, buildNumber: Int, toggled: Boolean) = AuthorizedComponent {
    repository =>
      val optionBranch = repository.branchRepository.getBranch(branchId)
      val optionBuild = optionBranch.flatMap(repository.buildRepository.toggleBuild(_, buildNumber, toggled))

      optionBuild.foreach(repository.notificationService.notifyToggle(optionBranch.get, _))

      request => Ok(Json.toJson(optionBuild))
  }

  def build(branch: String, number: Int) = AuthorizedComponent {
    registry =>
      val branchEntity = registry.branchRepository.getBranch(branch)
      val build = branchEntity.map(registry.buildRepository.getBuild(_, number))
      request => Ok(Json.toJson(build))
  }

  def run(branch: String, build: Int, part: String, run: String) = AuthorizedComponent {
    component =>

      val branchEntity = component.branchRepository.getBranch(branch)
      val runEntity = branchEntity.map(component.jenkinsService.getTestRun(_, build, part, run))
      request => Ok(Json.toJson(runEntity))
  }

  def testCase(branch: String, build: Int, part: String, run: String, test: String) = AuthorizedComponent {
    component =>

      val branchEntity = component.branchRepository.getBranch(branch)
      val buildNode: Option[BuildNode] = branchEntity.flatMap(component.jenkinsService.getTestRun(_, build, part, run))
      val testCase = buildNode.flatMap(n => n.getTestCase(test))
      request => Ok(Json.toJson(testCase))
  }

  def artifact(file: String) = AuthorizedComponent {
    component =>
      request => Ok.sendFile(content = component.jenkinsService.getArtifact(file))
  }

  def buildActions(branchName: String, buildNumber: Option[Int]) = AuthorizedComponent {
    implicit component =>
      request =>
        val maybeBranch: Option[Branch] = component.branchRepository.getBranch(branchName)
        val maybeBuild: Option[Build] = for (b <- maybeBranch; id <- buildNumber; build <- component.buildRepository.getBuild(b, id)) yield build

        val actions = (maybeBranch, maybeBuild) match {
          case (_, Some(build)) => component.jenkinsService.getBuildActions(build) ++ component.deployService.getDeployBuildActions(build)
          case (Some(branch), _) => branch.buildActions(component) ++ List(TransifexBuildAction(branch.name))
          case _ => Nil
        }

        Ok(Json.toJson(actions))
  }

  def lastBuilds(branch: String, count: Int) = AuthorizedComponent {
    component =>
      request =>
        Ok(Json.toJson(component.buildRepository.getLastBuilds(branch, count)))
  }

  def buildStatus(id: Int) = AuthorizedComponent {
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

  def deployBuild(buildId: String, destination: String) = AuthorizedComponent {
    component =>
      request => {
        for (
          build <- component.buildRepository.getBuild(buildId);
          deployConfig <- component.config.buildConfig.deploy.find(_.name == destination)
        ) {
          component.notificationService.notifyStartDeploy(deployConfig.channel, deployConfig.name, build)
          val deploy = component.deployService.deployBuild(buildId, deployConfig.deployTo)
          deploy.onComplete(component.notificationService.notifyDoneDeploy(deployConfig.channel, deployConfig.name, build, _))
          deploy.onFailure { case e => play.Logger.error("Error during deploy", e) }
        }

        Ok(Json.obj("message" -> "Ok"))
      }
  }

  case class UpdateInfo(name: Option[String], url: Option[String], build: Option[BuildInfo])

  case class BuildInfo(full_url: Option[String], number: Option[Int], phase: Option[String], status: Option[String], url: Option[String], parameters: Option[Map[String, String]])

  implicit val buildInfoRead = Json.reads[BuildInfo]
  implicit val updateInfoRead = Json.reads[UpdateInfo]

  def updateBuild() = Component {
    component =>
      request => {
        request.body.asJson.flatMap { json =>
          val params = json.as[UpdateInfo]

          // \\JM2\Artifacts\pr_3219_21298\Artifacts
          val artifactsPath = for (buildInfo <- params.build;
                                   parameters <- buildInfo.parameters;
                                   artifacts <- parameters.get("ARTIFACTS")
          ) yield Paths.get(artifacts)

          // \\jm2\Artifacts
          val root = Paths.get(component.config.jenkinsDataPath)

          val buildName = artifactsPath.map(root.relativize(_).subpath(0, 1).toString)

          buildName.foreach(component.buildWatcher.forceUpdate)

          buildName
        }
          .map(buildName => Ok(Json.obj("message" -> "Ok", "build" -> buildName)))
          .getOrElse(Ok(Json.obj("message" -> "Error updating build")))
      }
  }
}

