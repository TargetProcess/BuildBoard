package components

import models.{Build, BuildInfo, Branch}

trait BuildServiceComponent {
  val buildService: BuildService

  trait BuildService {
    def canBeToggled(build:Build):Boolean

    def toggleBuild(branch: Branch, number: Int, toggled: Boolean): Option[BuildInfo]
  }

}


