package components

import models.BuildInfo

trait BuildRepositoryComponent {
  def buildRepository: BuildRepository

  trait BuildRepository {
    def getBuildInfos: List[BuildInfo]
  }

}
