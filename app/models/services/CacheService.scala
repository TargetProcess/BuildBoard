package models.services

import scala.concurrent.duration._
import scala.util.Try
import rx.lang.scala.Observable
import models._

import src.Utils._
import rx.lang.scala.subscriptions.Subscription
import models.github.{GithubBranches, GithubBranch, RealGithubRepository}
import scala.util.Success
import scala.util.Failure
import scala.Some
import play.api.Play
import play.api.Play.current
import models.jenkins.JenkinsAdapter

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

  def cacheBuilds(interval: Duration, collection: Collection[Build])(getValues: => List[Build]) = {
    Observable.interval(interval).map(tick =>
      Try {
        getValues
      }
    ).subscribe(tryResult => {
      tryResult match {
        case Success(data) =>
          println("reload builds cache")
          val allItems = collection.findAll.toList

          //update items that are in progress
          val inProgressExisting = allItems.filter(!_.status.isDefined).toList
          val inProgressUpdated = data.filter(i => inProgressExisting.exists(e => e.number == i.number)).toList
          inProgressExisting.foreach(collection.remove)
          inProgressUpdated.foreach(collection.save)

          //insert new items
          val newItems = data.filterNot(i => allItems.exists(a => a.number == i.number))
          newItems.foreach(collection.save)

        case Failure(e) => play.Logger.error("Error", e)
      }
    })
  }

  val user =  Play.configuration.getString("cache.user").get
  val pullRequestInterval = Play.configuration.getInt("cache.interval.pullRequests").getOrElse(30).seconds
  val githubBranchesInterval = Play.configuration.getInt("cache.interval.githubBranches").getOrElse(10).seconds
  val jenkinsBuildsInterval = Play.configuration.getInt("cache.interval.jenkinsBuilds").getOrElse(60).seconds

  def start = {
    User.findOneByUsername(user) match {
      case Some(u) =>
        implicit val user = u
        val repo = new RealGithubRepository()

        val sub1 = cache[PullRequest](pullRequestInterval, PullRequests) {
          watch("cache: get pull requests") {
            repo.getPullRequests
          }
        }

        val sub2 = cache[GithubBranch](githubBranchesInterval, GithubBranches) {
          watch("cache: get github branches") {
            repo.getBranches
          }
        }

        val sub3 = cacheBuilds(jenkinsBuildsInterval, Builds) {
          watch("cache: get builds") {
            JenkinsAdapter.getBuilds
          }
        }

        Subscription {
          sub1.unsubscribe()
          sub2.unsubscribe()
          sub3.unsubscribe()
        }

      case None => play.Logger.error(s"Could not find user $user for cache service"); Subscription {}
    }
  }

}
