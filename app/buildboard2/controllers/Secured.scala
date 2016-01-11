package buildboard2.controllers

import buildboard2.components.{DefaultComponent, DefaultRegistry}
import play.api.mvc._

trait Secured {
  //todo: pass token explicitly from action
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

  def ToolTokenAuthenticatedComponent(token: String)(f: => DefaultComponent => Request[AnyContent] => Result): EssentialAction = Action {
    implicit request => {
      val component = new DefaultRegistry
      component.accountRepository.findByToken(token)
        .map(_ => f(component)(request))
        .getOrElse(Results.Status(401))
    }
  }
}
