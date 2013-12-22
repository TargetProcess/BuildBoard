package models.github

import scala.concurrent.duration._
import scala.concurrent.{Future, ExecutionContext}
import scala.util.Success
import scala.util.Failure
import rx.lang.scala.Observable
import rx.lang.scala.subjects.PublishSubject
import models.{PullRequests, User}
import play.Logger
import src.Utils._
import rx.lang.scala.subscriptions.Subscription


object GithubService {
  def start(checkPeriod: Duration) = {
    User.findOneByUsername("alex.fomin@targetprocess.com") match {
      case Some(u) =>
        implicit val user = u
        val repo = new RealGithubRepository()




        Observable.interval(30 seconds)
          .map(tick => watch("Get pull requests in service") {
          repo.getPullRequests
        })
          .subscribe(pullRequests => {
          PullRequests.findAll().foreach(PullRequests.remove)
          pullRequests.foreach(PullRequests.save)
        })

        Observable.interval(10 seconds)
          .map(tick => watch("Get branches in service") {
          repo.getBranches
        })
          .subscribe(branches => {
          GithubBranches.findAll().foreach(GithubBranches.remove)
          branches.foreach(GithubBranches.save)
        })


      case None => Subscription {}
    }
  }

}
