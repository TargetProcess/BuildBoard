package buildboard2.controllers

import buildboard2.Writes2
import play.api.mvc.Controller
import Writes2._

object Artifacts extends Controller with SecureAuthentication with SecurePaging {
  def builds(take: Option[Int], skip: Option[Int], token: String) = SecurePage(take, skip, token) {
    component => Page(component.artifact2Repository.getAll, component.artifact2Repository.count)
  }
}
