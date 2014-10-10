package components

import models.Build

trait RerunRepositoryComponent {
  val rerunRepository: RerunRepository

  trait RerunRepository {
    def contains(build: Build, category: String, part: String): Boolean

    def markAsRerun(build: Build, category: String, parts: List[String])
  }
}
