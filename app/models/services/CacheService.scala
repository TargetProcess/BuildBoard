package models.services

import scala.concurrent.duration._
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Try, Success, Failure}
import rx.lang.scala.Observable
import rx.lang.scala.subjects.PublishSubject
import models._

import src.Utils._
import rx.lang.scala.subscriptions.Subscription
import models.github.{GithubBranches, GithubBranch, RealGithubRepository}
import scala.util.Success
import scala.util.Failure
import scala.Some
import models.jenkins.{JenkinsRepository, JenkinsAdapter}


object CacheService {
  def cache[T](interval: Duration, collection: Collection[T])(getValues: => List[T]) = {
    Observable.interval(interval).map(tick =>
      Try {
        getValues
      }
    ).subscribe(tryResult => {
      tryResult match {
        case Success(data) =>
//          println("reload cache")
          collection.findAll.foreach(collection.remove)
          data.foreach(collection.save)
        case Failure(e) => play.Logger.error("Error", e)
      }
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
          println(s"existing in progress are $inProgressExisting")
          inProgressExisting.foreach(collection.remove)
          println(s"new in progress are $inProgressUpdated")
          inProgressUpdated.foreach(collection.save)

          //insert new items
          val newItems = data.filterNot(i => allItems.exists(a => a.number == i.number))
          println(s"new items are $newItems")
          newItems.foreach(collection.save)

//          collection.findAll.foreach(collection.remove)
//          data.foreach(collection.save)

        case Failure(e) => play.Logger.error("Error", e)
      }
    })
  }


  def start = {
    User.findOneByUsername("kanban@targetprocess.com") match {
      case Some(u) =>
        implicit val user = u
        val repo = new RealGithubRepository()

        val sub1 = cache[PullRequest](30 seconds, PullRequests) {
          watch("cache: get pull requests") {
            repo.getPullRequests
          }
        }

        val sub2 = cache[GithubBranch](10 seconds, GithubBranches) {
          watch("cache: get github branches") {
            repo.getBranches
          }
        }

        val sub3 = cacheBuilds(60 seconds, Builds) {
          watch("cache: get builds") {
            JenkinsAdapter.getBuilds
          }
        }


        Subscription {
          sub1.unsubscribe()
          sub2.unsubscribe()
          sub3.unsubscribe()
        }

      case None => Subscription {}
    }
  }

}
