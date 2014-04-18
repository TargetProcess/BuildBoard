package components

import models.{Build, Branch, BuildInfo}

trait BuildRepositoryComponent {
  val buildRepository: BuildRepository

  trait BuildRepository {
    def getBuildInfos: List[BuildInfo]
    def removeAll(branch:Branch)
    def update(branch:Branch, build:Build)
    def getBuilds(branch: Branch): List[Build]
    def toggleBuild(branch: Branch, number: Int, toggled: Boolean): Option[BuildInfo]
    def getBuild(branch: Branch, number: Int): Option[Build]
  }

}
