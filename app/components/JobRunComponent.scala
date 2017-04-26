package components

import models.JobRun
import org.joda.time.DateTime

trait JobRunComponent {
  val jobRunRepository: JobRunRepository

  trait JobRunRepository {
    def update(build: JobRun)

    def getJobRuns: Iterator[JobRun]

    def getJobRuns(buildNumber: Int): Iterator[JobRun]

    def getJobRuns(buildNumber: Int, buildNode: String): Iterator[JobRun]

    def removeOld(before: DateTime)
  }
}
