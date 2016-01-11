package buildboard2.controllers

import buildboard2.Writes2
import play.api.mvc.Controller
import Writes2._

object Jobs extends Controller with SecureAuthentication with SecurePaging {
  def jobs(take: Option[Int], skip: Option[Int], token: String) = SecurePage(take, skip, token) {
    component => Page(component.job2Repository.getAll, component.job2Repository.count)
  }
}
