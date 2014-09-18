package components

import models.Build
import models.branches.Branch

trait BuildRepositoryComponent {
  val buildRepository: BuildRepository

  trait BuildRepository {
    def getLastBuilds: Map[String, Build]

    def getAllBuilds: Iterator[Build]
    def removeAll(branch:Branch)
    def update(build:Build)
    def getBuilds(branch: Branch): Iterator[Build]
    def getBuilds(branch: String): Iterator[Build]

    def getLastBuild(branch: Branch):Option[Build]

    def toggleBuild(branch: Branch, number: Int, toggled: Boolean): Option[Build]
    def getBuild(branch: Branch, number: Int): Option[Build]
  }

}
