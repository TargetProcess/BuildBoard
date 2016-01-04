package buildboard2.controllers

import buildboard2.BuildInfo
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.api.Play.current
import buildboard2.controllers.Writes2._

object Builds extends Controller with Secured {
  def builds(take: Integer) = ToolTokenAuthenticatedComponent {
    component =>
      implicit request => {
        val builds = component.buildRepository.getBuilds
          .take(take)
          .toList
          .map(b => BuildInfo(b.number.toString,
            timestamp = b.timestamp,
            b.number,
            b.node.map(_.statusUrl).getOrElse(""),
            Map.empty,
            b.initiator,
            if (b.pullRequestId.isEmpty) Some(b.branch) else None,
            b.pullRequestId,
            b.status,
            b.ref))
        Ok(Json.toJson(Map("items" -> builds)))
      }
  }
}
