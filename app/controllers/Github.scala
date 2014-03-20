package controllers

import play.api.mvc._
import play.api.libs.json._
import Writes._
import models.github.GithubRepository
import models.services.CacheService
import scala.util.{Try, Success, Failure}
import models._
import models.PullRequest
import models.User
import scala.util.Success
import scala.util.Failure
import scala.Some


object Github extends Controller with Secured {

  def merge(branchName: String) = IsAuthorized {
    user =>
      implicit request =>
        implicit val authInfo = CacheService.authInfo
        val branches = new BranchRepository()

        branches.getBranch(branchName) match {
          case None => NotFound(Json.obj("message" -> s"Branch $branchName is not found"))
          case Some(branch) =>
            branch.pullRequest match {
              case None => BadRequest(Json.obj("message" -> s"There is no pull request for branch $branchName"))
              case Some(pullRequest) => mergeAndDelete(authInfo, user, branch, pullRequest)
            }
        }
  }

  private def mergeAndDelete(implicit authInfo:AuthInfo, user: User, branch: Branch, pullRequest: PullRequest) = {
    val repo = new GithubRepository()

    val tryMerge = Try {
      repo.mergePullRequest(pullRequest.prId, user)
    }
    val tryDelete = tryMerge.map(_ => Try {
      repo.deleteBranch(branch.name)
    })

    (tryMerge, tryDelete) match {
      case (Failure(e), _) => InternalServerError(Json.obj(
        "message" -> s"Could not merge PR#${pullRequest.prId}",
        "exception" -> e.toString
      ))
      case (Success(mergeStatus), Failure(e)) => InternalServerError(Json.obj(
        "message" -> s"PR#${pullRequest.prId} is merged, but branch ${branch.name} is not deleted",
        "merged" -> true,
        "exception" -> e.toString
      ))
      case (Success(mergeStatus), Success(_)) => Ok(Json.obj(
        "message" -> mergeStatus.message,
        "merged" -> true
      ))
    }
  }
}
