package controllers

import play.api.mvc._
import models.User

trait Secured {

  private def username(request: RequestHeader) = request.session.get("login")
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Login.index)

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) =
    Security.Authenticated(username, onUnauthorized) {
      user => Action(request => f(user)(request))
    }

  def IsAuthorized(f: => User => Request[AnyContent] => Result) =
    Security.Authenticated(username, onUnauthorized) {
      username => 
        User.findOneByUsername(username).map { user =>
         Action(request=>f(user)(request))
        }.getOrElse(Action(onUnauthorized _))
    }
}