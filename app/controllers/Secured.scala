package controllers

import play.api.mvc._
import models._
import play.api.mvc.BodyParsers.parse
import components.{DefaultRegistry, Registry, DefaultComponent}

trait Secured {

  private def username(request: RequestHeader) = request.session.get("login")

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Login.index())

  def IsAuthenticated[A](bodyParser: BodyParser[A])(f: => String => Request[A] => Result) =
    Security.Authenticated(username, onUnauthorized) {
      user => Action(bodyParser)(request => f(user)(request))
    }

  def IsAuthorized[A](bodyParser: BodyParser[A])(f: => User => Request[A] => Result) =
    IsAuthenticated(bodyParser) {
      username => {
        Registry.userRepository.findOneByUsername(username) match {
          case Some(user) => request => f(user)(request)
          case None => request => onUnauthorized(request)
        }
      }
    }

  def AuthorizedComponent(f: => DefaultComponent => Request[AnyContent] => Result): EssentialAction = AuthorizedComponent(parse.anyContent)(f)

  def AuthorizedComponent[A](bodyParser: BodyParser[A])(f: => DefaultComponent => Request[A] => Result) =
    IsAuthorized(bodyParser) {
      user => {
        val component = new DefaultRegistry(user)
        request => f(component)(request)
      }
    }

  def IsAuthorized(f: => User => Request[AnyContent] => Result): EssentialAction = IsAuthorized(parse.anyContent)(f)

  def IsAuthenticated(f: => String => Request[AnyContent] => Result): EssentialAction = IsAuthenticated(parse.anyContent)(f)

  def Component(f: => DefaultComponent => Request[AnyContent] => Result): EssentialAction = Component(parse.anyContent)(f)

  def Component[A](bodyParser: BodyParser[A])(f: => DefaultComponent => Request[A] => Result) = {
    Action(bodyParser)(request => f(DefaultRegistry)(request))
  }
}