package buildboard2.controllers

import buildboard2.Writes2
import buildboard2.model.BuildInfo
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.api.Play.current
import Writes2._

object Builds extends Controller with Secured {
  def builds(take: Integer) = ToolTokenAuthenticatedComponent {
    component =>
      implicit request => {
        val builds = component.buildRepository.getBuilds
          .take(take)
          .toList
          .map(b => new BuildInfo(b))
        Ok(Json.toJson(Map("items" -> builds)))
      }
  }
}
