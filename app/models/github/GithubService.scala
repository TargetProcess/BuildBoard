package models.github
import scala.concurrent.duration._
import scala.concurrent.{Future, ExecutionContext}
import scala.util.Success
import scala.util.Failure
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject
import models.User


object GithubService {
  def start(checkPeriod:Duration){
    implicit val admin = User.findOneByUsername("fomin")

  }
}
