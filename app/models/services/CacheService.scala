package models.services

import components.DefaultRegistry
import models.Build
import org.joda.time.DateTime
import rx.lang.scala.{Observable, Subscription}
import src.Utils.watch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object CacheService {

  val registry = DefaultRegistry

  val jenkinsDataPath: String = registry.config.jenkinsDataPath

  val githubInterval = registry.config.githubInterval
  val jenkinsInterval = registry.config.jenkinsInterval


  def start = {

    val jenkinsSubscription: Future[Subscription] = Future {
      updateBuilds(Nil)
    }.map(_ => subscribeToJenkins)
    val githubSubscription = subscribeToGithub

    Subscription {
      githubSubscription.unsubscribe()
      jenkinsSubscription.onSuccess { case x => x.unsubscribe() }
    }
  }

  def subscribeToJenkins: Subscription = {
    play.Logger.info("Starting Jenkins subscription")

    val buildObservable = registry.buildWatcher.start

    play.Logger.info("Started Jenkins subscription")

    buildObservable
      .buffer(jenkinsInterval)
      .map(_.distinct)
      .subscribe(buildNames =>
        try {
          updateBuilds(buildNames)
        }        
        catch {
          case e : Throwable => play.Logger.error("Error in Jenkins subscription", e)
        }
      )
  }

  def updateBuilds(updatedBuildNames: Seq[String]) = watch(s"update builds: ${updatedBuildNames.length}") {
    val existingBuilds = registry.buildRepository.getBuilds.toList
    play.Logger.info(s"existingBuilds: ${existingBuilds.length}")

    val updatedBuilds: Stream[Build] = registry.jenkinsService.getUpdatedBuilds(existingBuilds, updatedBuildNames)

    play.Logger.info("got updated builds")

    var updatedBuildsNum = 0

    for (updatedBuild <- updatedBuilds) {
        try {
          registry.buildRepository.update(updatedBuild)
          registry.buildRerun.rerunFailedParts(updatedBuild)
          registry.jobRunRepository.removeOld(DateTime.now().minusDays(7))

          updatedBuildsNum += 1
        }
        catch {
          case e : Throwable => play.Logger.error("Error in Update builds", e)
        }
    }

    play.Logger.info(s"${updatedBuildsNum} builds were updated, notifying")

    registry.notificationService.notifyAboutBuilds(updatedBuilds.toIterator)
  }

  def subscribeToGithub: Subscription = {
    Observable.timer(0 seconds, githubInterval)
      .map(_ => Try {
        registry.branchService.getBranches
      })
      .subscribe({
        case Success(data) =>
          val branches = registry.branchRepository.getBranches

          Try {
            branches
              .filter(b => !data.exists(_.name == b.name))
              .foreach(branch => {
                registry.branchRepository.remove(branch)
                registry.buildRepository.removeAll(branch)
              })
          }

          Try {
            data.foreach(branch => registry.branchRepository.update(branch))
          }

        case Failure(e) => play.Logger.error("Error when get branches from Github", e)
      },
        error => {
          play.Logger.error("Error in Github subscription", error)
        })
  }
}
