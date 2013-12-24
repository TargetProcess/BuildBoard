package models.jenkins

import models.{Builds, Build, Branch}


trait JenkinsRepository {
  def getBuilds:List[models.Build]

  def getBuilds(branch: models.Branch): List[models.Build] =  branch match {
    case models.Branch(name, _, pullRequest, _, _) =>
      val pullRequestId = pullRequest.map(p => p.prId)
      getBuilds.filter((build: models.Build) => build.branch == name || build.branch == s"origin/$name" || (pullRequestId.isDefined && build.branch == s"origin/pr/${pullRequestId.get}/merge"))
  }


  def getLastBuildsByBranch(branches: List[models.Branch]): Map[String, Option[models.Build]] = {
    val builds = getBuilds
    branches.map(b => {
      val pullRequestId = b.pullRequest.map(p => p.prId)
      val branchBuilds = builds.filter(build => build.branch == b.name || build.branch == s"origin/${b.name}" || (pullRequestId.isDefined && build.branch == s"origin/pr/${pullRequestId.get}/merge"))
      (s"origin/${b.name}", branchBuilds.headOption)
    })
      .toMap
  }

  def getLastBuild(branch: models.Branch): Option[models.Build] = getBuilds(branch).headOption
  def getBuild(branch: models.Branch, number: Int): Option[models.Build] = getBuilds(branch).find(_.number == number)
}

object CachedJenkinsRepository extends JenkinsRepository {
  def getBuilds: List[Build] = Builds.findAll().toList
}




