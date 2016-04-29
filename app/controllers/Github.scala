package controllers

import controllers.Formats._
import models.services.CacheService
import play.Play
import play.api.libs.json._

import scala.util.{Failure, Success}

object Github extends Application {

  def merge(branchName: String) = AuthorizedComponent {
    registry =>
      implicit request =>

        val mergeResult = CacheService.registry.magicMergeService.merge(branchName, registry.loggedUser.get)

        mergeResult match {
          case Success(reason) => Ok(Json.toJson(reason))
          case Failure(e) => BadRequest(Json.obj("message" -> e.toString))
        }
  }

  def prStatus(id: Int) = AuthorizedComponent {
    component =>
      request => {
        val branch = component.branchRepository.getBranchByEntity(id)
        val status: Option[Boolean] = branch.flatMap(_.pullRequest).map(_.status.isMergeable)

        val fileName = status match {
          case None => "unknown"
          case Some(true) => "ok"
          case Some(false) => "fail"
        }

        val file = Play.application.getFile(s"public/images/pr/$fileName.png")

        Ok.sendFile(file, inline = true)
      }
  }

  def pullRequest(entityId: Int) = AuthorizedComponent {
    component =>
      implicit request => {
        val branch = component.branchRepository.getBranchByEntity(entityId)
        println(branch)

        if (branch.isDefined) {
          val pr = branch.flatMap(_.pullRequest).map(_.url)
          println(pr)
          if (pr.isDefined) {
            Redirect(pr.get)
          }
          else {
            Redirect(branch.get.url)
          }
        }
        else {
          Redirect(routes.Landing.index())
        }
      }
  }
}
