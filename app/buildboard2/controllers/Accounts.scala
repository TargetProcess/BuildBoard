package buildboard2.controllers

import buildboard2.Account
import play.api.libs.json.Json
import buildboard2.controllers.Writes2._
import buildboard2.controllers.Reads2._
import play.api.mvc.Controller

object Accounts extends Controller with Secured {
  val secretToken = "bbftw"

  def save(toolToken: String) = TokenAuthenticatedComponent(secretToken) {
    component =>
      implicit request =>
        val account = request.body.asJson.get.as[Account]

        component.accountRepository.remove(toolToken)
        component.accountRepository.save(account)

        Created(Json.toJson(account))
  }

  def account(toolToken: String) = TokenAuthenticatedComponent(secretToken) {
    component =>
      implicit request =>
        val account = component.accountRepository.findByToken(toolToken)
        Ok(Json.toJson(account))
  }

  def delete(toolToken: String) = TokenAuthenticatedComponent(secretToken) {
    component =>
      implicit request =>
        val account = component.accountRepository.remove(toolToken)
        Ok(Json.toJson(Map("result" -> "deleted")))
  }
}
