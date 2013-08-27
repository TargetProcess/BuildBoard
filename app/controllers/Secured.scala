package controllers

import play.api.mvc._

trait Secured {

  private def username(request: RequestHeader) = request.session.get("login")
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Login.index)

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) =
    Security.Authenticated(username, onUnauthorized) {
      user => Action(request => f(user)(request))
    }
}