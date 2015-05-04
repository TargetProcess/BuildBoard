package components

import models.{Branch, Build}

trait BuildRepositoryComponent {
  val buildRepository: BuildRepository

  trait BuildRepository {
    def removeAll(branch: Branch)

    def update(build: Build)

    def getBuilds: Iterator[Build]

    def getBuilds(branch: Branch, limit: Int): Iterator[Build]

    def getBuilds(branch: String): Iterator[Build]

    def getLastBuilds(branch: String, count: Int = 1): List[Build]

    def getLastBuilds: Map[String, Build]

    def toggleBuild(branch: Branch, number: Int, toggled: Boolean): Option[Build]

    def getBuild(branch: Branch, number: Int): Option[Build]

    def getBuild(name: String): Option[Build]
  }

}
