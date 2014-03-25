package models.services

import scala.concurrent.duration._
import scala.util.Try
import rx.lang.scala.Observable
import rx.lang.scala.subscriptions.Subscription
import scala.util.Success
import scala.util.Failure
import play.api.Play
import play.api.Play.current
import models.mongo.{Builds, Branches}
import com.mongodb.casbah.commons.MongoDBObject
import models.{BuildRepository, AuthInfo}
import src.Utils.watch
import models.jenkins.JenkinsRepository

object CacheService {
  val githubInterval = Play.configuration.getInt("github.cache.interval").getOrElse(600).seconds
  val jenkinsInterval = Play.configuration.getInt("jenkins.cache.interval").getOrElse(60).seconds
  val authInfo = (for {
    tpToken <- Play.configuration.getString("cache.user.tp.token")
    gToken <- Play.configuration.getString("cache.user.github.token")
  }
  yield new AuthInfo {
      override val githubToken: String = gToken
      override val token: String = tpToken
    }).get


  def start = {
    val branchesService = new BranchService(authInfo)
    val jenkinsRepository = new JenkinsRepository()
    val githubSubscription = Observable.interval(githubInterval)
      .map(_ => Try {
      branchesService.getBranches
    })
      .subscribe(tryResult => tryResult match {
      case Success(data) =>
        val branches = Branches.findAll().toList
        watch("removing obsolete branches") {
          Try {
            branches
              .filter(b => !data.exists(_.name == b.name))
              .foreach(branch => {
              Branches.remove(branch)

              Builds.find(MongoDBObject("branch" -> branch.name))
                .foreach(Builds.remove)
            })
          }
        }
        watch("updating branches") {
          Try {
            data.foreach(branch => Branches.update(MongoDBObject("name" -> branch.name), branch, upsert = true, multi = false, Branches.dao.collection.writeConcern))
          }
        }
      case Failure(e) => play.Logger.error("Error", e)
    })
    val jenkinsSubscription = Observable.interval(jenkinsInterval)
    .subscribe(_ => Try {
      val branches = Branches.findAll().toList

      watch("updating builds") {
        branches.foreach(b => {
          val existingBuilds = new BuildRepository().getBuilds(b)
          val builds = jenkinsRepository.getBuilds(b)
          builds.foreach(build => {
            val existingBuild = existingBuilds.find(_.number == build.number)
            val toggled = if (existingBuild.isDefined) existingBuild.get.toggled else build.toggled
            Builds.update(MongoDBObject("number" -> build.number, "branch" -> b.name), build.copy(toggled = toggled), upsert = true, multi = false, Builds.dao.collection.writeConcern)
          })
        })
      }
    })

    Subscription {
      githubSubscription.unsubscribe()
      jenkinsSubscription.unsubscribe()
    }
  }
}
