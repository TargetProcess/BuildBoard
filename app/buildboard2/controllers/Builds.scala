package buildboard2.controllers

import buildboard2.Writes2
import play.api.libs.json.Json
import play.api.mvc.Controller
import Writes2._

object Builds extends Controller with Secured with Pageable {
  def builds(take: Option[Int], skip: Option[Int], token: String) = SecurePage(take, skip, token) {
    component => Page(component.build2Repository.getAll, component.build2Repository.count)
  }
}
