package components

import models.{Build, Branch}

trait BuildRepositoryComponent {
  val buildRepository: BuildRepository

  trait BuildRepository {
    def getBuildInfos: Iterator[Build]
    def removeAll(branch:Branch)
    def update(build:Build)
    def getBuilds(branch: Branch): Iterator[Build]

    def getLastBuild(branch: Branch):Option[Build]

    def toggleBuild(branch: Branch, number: Int, toggled: Boolean): Option[Build]
    def getBuild(branch: Branch, number: Int): Option[Build]
  }

}
