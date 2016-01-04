package buildboard2.controllers

import buildboard2.components.{DefaultComponent, DefaultRegistry}
import play.api.mvc._

trait Secured {
  def TokenAuthenticatedComponent(token: String)(f: => DefaultComponent => Request[AnyContent] => Result): EssentialAction = Action {
    implicit request => {
      val component = new DefaultRegistry
      val result = request.queryString("token")
        .find(tkn => tkn == token)
        .map(_ => f(component)(request))
        .getOrElse(Results.Status(401))

      result
    }
  }

  def ToolTokenAuthenticatedComponent(f: => DefaultComponent => Request[AnyContent] => Result): EssentialAction = Action {
    implicit request => {
      val component = new DefaultRegistry
      val result = request.queryString("token")
        .find(tkn => component.accountRepository.findByToken(tkn).isDefined)
        .map(_ => f(component)(request))
        .getOrElse(Results.Status(401))

      result
    }
  }
}
