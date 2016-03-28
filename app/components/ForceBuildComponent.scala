package components

import models.buildActions.JenkinsBuildAction

import scala.util.Try

trait ForceBuildComponent {
  val forceBuildService: ForceBuildService

  trait ForceBuildService {
    def forceBuild(action: JenkinsBuildAction): Try[Any]
  }

}
