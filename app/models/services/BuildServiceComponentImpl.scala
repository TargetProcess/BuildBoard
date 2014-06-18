package models.services

import components._
import models.BuildImplicits._
import models.{ToggleInfo, BuildInfo, Branch}
import org.joda.time.DateTime


trait BuildServiceComponentImpl extends BuildServiceComponent {
  this: BuildServiceComponentImpl
    with BuildRepositoryComponent
    with BranchRepositoryComponent
    with LoggedUserProviderComponent
  =>

  val buildService: BuildService = new BuildServiceImpl

  class BuildServiceImpl extends BuildService {
    def toggleBuild(branch: Branch, number: Int, toggled: Boolean): Option[BuildInfo] = {
      val build = buildRepository.getBuild(branch, number)
        .map(b => b.copy(toggle = if(toggled) Some(ToggleInfo(loggedUser.get, DateTime.now())) else None))

      build.foreach(buildRepository.update)

      build.map(toBuildInfo)
    }
  }


}