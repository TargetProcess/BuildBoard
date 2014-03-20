package models.services

import models.{Build, BuildInfo, Branch}
import models.jenkins.JenkinsRepository

class BuildService {
  val jenkinsRepository = new JenkinsRepository

  def getBuilds(branch: Branch): List[Build] = {
    branch.activity
      .map {
      case b: BuildInfo => Some(b)
      case _ => None
    }
      .flatten
      .flatMap(buildInfo => jenkinsRepository.getBuild(branch, buildInfo.number))
  }
}
