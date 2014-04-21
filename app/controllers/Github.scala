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
import components.DefaultComponent

object Github extends Application {

  def merge(branchName: String) = IsAuthorized {
    user =>
      implicit request =>

        val branches = CacheService.registry.branchRepository

        branches.getBranch(branchName) match {
          case None => NotFound(Json.obj("message" -> s"Branch $branchName is not found"))
          case Some(branch) =>
            branch.pullRequest match {
              case None => BadRequest(Json.obj("message" -> s"There is no pull request for branch $branchName"))
              case Some(pullRequest) => mergeAndDelete(CacheService.registry, user, branch, pullRequest)
            }
        }
  }

  private def mergeAndDelete(repository: DefaultComponent, user: User, branch: Branch, pullRequest: PullRequest) = {

    val githubRepo = repository.githubRepository
    val entityRepo = repository.entityRepository

    val tryMerge = Try {
      githubRepo.mergePullRequest(pullRequest.prId, user)
    }
    val tryDelete = tryMerge.map(_ => Try {
      githubRepo.deleteBranch(branch.name)
    })


    val tryChangeState: Try[Option[EntityState]] = tryDelete match {
      case e@Failure(ex) => Failure(ex)
      case Success(_) =>
        val pair: Option[(Entity, EntityState)] = for (
          entity <- branch.entity;
          finalState <- entity.state.nextStates.find(_.isFinal)
        ) yield (entity, finalState)

        pair match {
          case None => Success(None)
          case Some((entity, finalState)) =>
            val result: Try[Option[EntityState]] = Try {
              Some(entityRepo.changeEntityState(entity.id, finalState.id))

            }
            result
        }
    }

    (tryMerge, tryDelete, tryChangeState) match {
      case (Failure(e), _, _) => InternalServerError(Json.obj(
        "message" -> s"Could not merge PR#${pullRequest.prId}",
        "exception" -> e.toString
      ))
      case (Success(mergeStatus), Failure(e), _) => InternalServerError(Json.obj(
        "message" -> s"PR#${pullRequest.prId} is merged, but branch ${branch.name} is not deleted",
        "merged" -> true,
        "exception" -> e.toString
      ))
      case (Success(mergeStatus), Success(_), Failure(e)) => Ok(Json.obj(
        "message" -> s"PR#${pullRequest.prId} is merged, branch ${branch.name} is deleted, but entity is not moved to final state",
        "merged" -> true
      ))
      case (Success(mergeStatus), Success(_), Success(Some(state))) => Ok(Json.obj(
        "message" -> s"PR#${pullRequest.prId} is merged, branch ${branch.name} is deleted, entity is moved to final state",
        "newState" -> state,
        "merged" -> true
      ))
      case (Success(mergeStatus), Success(_), Success(None)) =>
        Ok(Json.obj(
          "message" -> s"PR#${pullRequest.prId} is merged, branch ${branch.name} is deleted",
          "merged" -> true
        ))

    }
  }
}
