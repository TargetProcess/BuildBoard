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
import models.jenkins.RealJenkinsRepository


object CacheService {
  def cache[T](interval: Duration, collection: Collection[T])(getValues: => List[T]) = {
    Observable.interval(interval).map(tick =>
      Try {
        getValues
      }
    ).subscribe(tryResult => {
      tryResult match {
        case Success(data) =>
          collection.findAll.foreach(collection.remove)
          data.foreach(collection.save)
        case Failure(e) => play.Logger.error("Error", e)
      }
    })
  }


  def start = {
    User.findOneByUsername("alex.fomin@targetprocess.com") match {
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

        val sub3 = cache[Build](60 seconds, Builds) {
          watch("cache: get builds") {
            RealJenkinsRepository.getBuilds
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
