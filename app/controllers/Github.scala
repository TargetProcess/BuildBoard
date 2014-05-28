package controllers

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
}
