package models.services

import java.io.File

import components.DefaultRegistry
import models.AuthInfo
import models.jenkins.FileApi
import play.api.Play
import play.api.Play.current
import rx.lang.scala.{Observable, Subscription}
import src.Utils.watch

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object CacheService extends FileApi {
  val authInfo: AuthInfo = (for {
    tpToken <- Play.configuration.getString("cache.user.tp.token")
    gToken <- Play.configuration.getString("cache.user.github.token")
  }

  yield new AuthInfo {
      override val githubToken: String = gToken
      override val token: String = tpToken
    }).get


  val registry = new DefaultRegistry(authInfo)

  val githubInterval = Play.configuration.getMilliseconds("github.cache.interval").getOrElse(600000L).milliseconds
  val jenkinsInterval = Play.configuration.getMilliseconds("jenkins.cache.interval").getOrElse(60000L).milliseconds


  def start = {
    val artifactsDir = new File(directory)

    val dir_watcher = new DirectoryWatcher(artifactsDir, true)

    val jenkinsSubscription = dir_watcher.observable.map(file => {
      val directoryName = artifactsDir.toPath.relativize(file.toPath).subpath(0, 1)
      directoryName.toString
    }).buffer(jenkinsInterval)
      .subscribe(fileChangedEvents => Try {
      watch("updating builds") {
        val existingBuilds = registry.buildRepository.getBuilds.toList
        play.Logger.info(s"existingBuilds: ${existingBuilds.length}")
        play.Logger.info(s"files changed: ${fileChangedEvents.length}")

        val buildToUpdate = registry.jenkinsService.getUpdatedBuilds(existingBuilds, fileChangedEvents)
        play.Logger.info(s"buildToUpdate: ${buildToUpdate.length}")

        for (updatedBuild <- buildToUpdate) {
          registry.buildRepository.update(updatedBuild)
        }

        registry.notificationService.notifyAboutBuilds(registry.buildRepository.getBuilds.toList)
      }
    }.recover {
      case e => play.Logger.error("Error in jenkinsSubscription", e)
    },
        error => {
          play.Logger.error("Error in jenkinsSubscription", error)
        })

    val githubSubscription = subscribeToGithub

    Subscription {
      githubSubscription.unsubscribe()
      jenkinsSubscription.unsubscribe()
    }
  }

  def subscribeToGithub: Subscription = {
    Observable.timer(0 seconds, githubInterval)
      .map(_ => Try {
      registry.branchService.getBranches
    })
      .subscribe({
      case Success(data) =>
        val branches = registry.branchRepository.getBranches
        watch("removing obsolete branches") {
          Try {
            branches
              .filter(b => !data.exists(_.name == b.name))
              .foreach(branch => {
              registry.branchRepository.remove(branch)
              registry.buildRepository.removeAll(branch)
            })
          }
        }
        watch("updating branches") {
          Try {
            data.foreach(branch => registry.branchRepository.update(branch))
          }
        }
      case Failure(e) => play.Logger.error("Error", e)
    },
    error => {
      play.Logger.error("Error in githubSubscription", error)
    })
  }
}
