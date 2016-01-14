package buildboard2.components

import buildboard2.model.Artifact2

trait Artifact2RepositoryComponent {
  val artifact2Repository: Artifact2Repository

  trait Artifact2Repository {
    def save(build: Artifact2): Unit

    def count: Long

    def getAll: Iterator[Artifact2]
  }
}
