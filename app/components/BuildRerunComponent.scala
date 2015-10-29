package components

import models.Build

trait BuildRerunComponent {
  val buildRerun: BuildRerun

  trait BuildRerun {
    def rerunFailedParts(updatedBuild:Build)
  }
}
