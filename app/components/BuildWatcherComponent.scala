package components

import models.Build

trait BuildWatcherComponent {
  val buildWatcher: BuildWatcher

  trait BuildWatcher {
    def rerunFailedParts(updatedBuild:Build)
  }
}
