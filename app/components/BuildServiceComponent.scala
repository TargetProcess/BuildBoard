package components

import models.{Build, BuildInfo, Branch}

trait BuildServiceComponent {
  val buildService: BuildService

  trait BuildService {
    def toggleBuild(branch: Branch, number: Int, toggled: Boolean): Option[BuildInfo]
  }

}


