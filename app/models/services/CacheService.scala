package models.services

import scala.concurrent.duration._
import scala.util.Try
import rx.lang.scala.Observable
import models._

import src.Utils._
import rx.lang.scala.subscriptions.Subscription
import models.github.RealGithubRepository
import scala.util.Success
import scala.util.Failure
import scala.Some
import play.api.Play
import play.api.Play.current
import models.mongo.{Users, GithubBranches, PullRequests, Collection}

object CacheService {
  def cache[T](interval: Duration, collection: Collection[T])(getValues: => List[T]) = {
    Observable.interval(interval).map(tick => Try { getValues })
      .subscribe(tryResult => tryResult match {
          case Success(data) =>
            collection.findAll.foreach(collection.remove)
            data.foreach(collection.save)
          case Failure(e) => play.Logger.error("Error", e)
        })
  }

  val user =  Play.configuration.getString("cache.user").get
  val pullRequestInterval = Play.configuration.getInt("cache.interval.pullRequests").getOrElse(30).seconds
  val githubBranchesInterval = Play.configuration.getInt("cache.interval.githubBranches").getOrElse(10).seconds

  def start = {
    Users.findOneByUsername(user) match {
      case Some(u) =>
        implicit val user = u
        val repo = new RealGithubRepository()

        val sub1 = cache[PullRequest](pullRequestInterval, PullRequests) {
          watch("cache: get pull requests") {
            repo.getPullRequests
          }
        }

        val sub2 = cache[Branch](githubBranchesInterval, GithubBranches) {
          watch("cache: get github branches") {
            repo.getBranches
          }
        }

        Subscription {
          sub1.unsubscribe()
          sub2.unsubscribe()
        }

      case None => play.Logger.error(s"Could not find user $user for cache service"); Subscription {}
    }
  }

}
