package buildboard2.components

import buildboard2.model.Job2

trait Job2RepositoryComponent {
  val job2Repository: Job2Repository

  trait Job2Repository {
    def save(job: Job2): Unit

    def remove(id: String) : Unit

    def count: Long

    def getAll: Iterator[Job2]
  }
}
