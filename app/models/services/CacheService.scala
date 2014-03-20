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
import models.AuthInfo
import src.Utils.watch

object CacheService {
  val githubBranchesInterval = Play.configuration.getInt("cache.interval").getOrElse(600).seconds
  val authInfo = (for (tpToken <- Play.configuration.getString("cache.user.tp.token");
                       gToken <- Play.configuration.getString("cache.user.github.token"))
  yield new AuthInfo {
      override val githubToken: String = gToken
      override val token: String = tpToken
    }).get


  def start = {
    val branchesService = new BranchService(authInfo)
    val buildService = new BuildService

    val observable = Observable(0) ++ Observable.interval(githubBranchesInterval)

    val subscription = observable
      .map(tick => Try {
      branchesService.getBranches
    })
      .subscribe(tryResult => tryResult match {
      case Success(data) => {
        watch("removing obsolete branches") {
          Branches.findAll()
            .filter(b => !data.exists(_.name == b.name))
            .foreach(branch => Branches.remove(branch))
        }
        watch("updating branches") {
          data.foreach(branch => {
            Branches.update(MongoDBObject("name" -> branch.name), branch, upsert = true, multi = false, Branches.dao.collection.writeConcern)
            val builds = buildService.getBuilds(branch)
            builds.foreach(build => Builds.update(MongoDBObject("number" -> build.number, "branch" -> branch.name), build, upsert = true, multi = false, Builds.dao.collection.writeConcern))
          })
        }
      }
      case Failure(e) => play.Logger.error("Error", e)
    })

    Subscription {
      subscription.unsubscribe()
    }
  }
}
