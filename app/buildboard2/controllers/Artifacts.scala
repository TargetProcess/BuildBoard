package buildboard2.controllers

import buildboard2.Writes2._
import buildboard2.components.DefaultRegistry
import play.api.mvc.{Controller, Action}

object Artifacts extends Controller with SecureAuthentication with SecurePaging {
  def artifacts(take: Option[Int], skip: Option[Int], token: String) = SecurePage(take, skip, token) {
    component => Page(component.artifact2Repository.getAll, component.artifact2Repository.count)
  }

  def artifact(file: String) = Action {
    request => {
      val component = new DefaultRegistry

      Ok.sendFile(content = component.artifact2Repository.getArtifact(file))
    }
  }
}
