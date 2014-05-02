package models.services

import scala.concurrent.duration._
import scala.util.Try
import rx.lang.scala.{Subscription, Observable}

import scala.util.Success
import scala.util.Failure
import play.api.Play
import play.api.Play.current
import models.AuthInfo
import src.Utils.watch
import components.DefaultRegistry


object CacheService {
  val authInfo: AuthInfo = (for {
    tpToken <- Play.configuration.getString("cache.user.tp.token")
    gToken <- Play.configuration.getString("cache.user.github.token")
  }

  yield new AuthInfo {
      override val githubToken: String = gToken
      override val token: String = tpToken
    }).get


  val registry = new DefaultRegistry(authInfo)

  val githubInterval = Play.configuration.getInt("github.cache.interval").getOrElse(600).seconds
  val jenkinsInterval = Play.configuration.getInt("jenkins.cache.interval").getOrElse(60).seconds


  def start = {
    val jenkinsRepository = registry.jenkinsService
    val githubSubscription = Observable.timer(0 seconds, githubInterval)
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

    val jenkinsSubscription = Observable.timer(0 seconds, jenkinsInterval)
      .subscribe(_ => Try {

      watch("updating builds") {
        val existingBuilds = registry.buildRepository.getBuildInfos.toList

        val updatedBuilds = registry.jenkinsService.getUpdatedBuilds(existingBuilds)

        for (updatedBuild <- updatedBuilds) {
          registry.buildRepository.update(updatedBuild)
        }

        registry.notificationService.notifyAboutBuilds(updatedBuilds)
      }
    }.recover {
      case e => play.Logger.error("Error in jenkinsSubscription", e)
    },
        error => {
          play.Logger.error("Error in jenkinsSubscription", error)
        })

    Subscription {
      githubSubscription.unsubscribe()
      jenkinsSubscription.unsubscribe()
    }
  }
}
