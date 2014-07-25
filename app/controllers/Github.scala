package controllers

import controllers.Jenkins._
import models.BuildStatus.{InProgress, Unknown}
import play.Play
import play.api.libs.json._
import Writes._
import models.services.CacheService
import scala.util.Try
import models._
import models.PullRequest
import models.User
import scala.util.Success
import scala.util.Failure
import scala.Some
import components.{DefaultRegistry, DefaultComponent}

object Github extends Application {

  def merge(branchName: String) = IsAuthorizedComponent {
    registry =>
      implicit request =>

        val mergeResult = CacheService.registry.magicMergeService.merge(branchName, registry.loggedUser.get)

        mergeResult match {
          case Success(reason)=> Ok(Json.toJson(reason))
          case Failure(e) => BadRequest(Json.obj("message" -> e.toString))
        }
  }

  def prStatus(id: Int) = IsAuthorizedComponent {
    component =>
      request => {
        val branch = component.branchRepository.getBranchEntity(id)
        val status:Option[Boolean] = branch.flatMap(_.pullRequest).map(_.status.isMergeable)

        val fileName = status match {
            case None => "unknown"
            case Some(true) => "ok"
            case Some(false) => "fail"
        }

        val file = Play.application.getFile(s"public/images/pr/$fileName.png")

        Ok.sendFile(file, inline = true)
      }
  }
}
