package buildboard2.controllers

import buildboard2.Writes2
import buildboard2.model.Build2
import play.api.mvc.Controller
import Writes2._

object Builds extends Controller with SecureAuthentication with SecurePaging {
  def builds(take: Option[Int], skip: Option[Int], token: String) = SecurePage(take, skip, token) {
    component => Page(component.buildRepository.getBuilds.map(Build2.create), component.buildRepository.count())
  }
}
