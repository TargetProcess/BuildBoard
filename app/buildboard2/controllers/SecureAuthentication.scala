package buildboard2.controllers

import buildboard2.components.{DefaultComponent, DefaultRegistry}
import play.api.Play
import play.api.Play.current
import play.api.mvc._

trait SecureAuthentication {
  val secretToken = Play.configuration.getString("buildboard2.token").get

  def TokenAuthenticatedComponent(token: String)(f: => DefaultComponent => Request[AnyContent] => Result): EssentialAction = Action {
    implicit request => {
      val component = new DefaultRegistry
      if (token == secretToken){
        f(component)(request)
      }
      else {
        Results.Status(401)
      }
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
