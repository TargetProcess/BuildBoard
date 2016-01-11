package buildboard2.components

import buildboard2.model.Build2

trait Build2RepositoryComponent {
  val build2Repository: Build2Repository

  trait Build2Repository {
    def save(build: Build2): Unit

    def count: Long

    def getAll: Iterator[Build2]
  }
}
