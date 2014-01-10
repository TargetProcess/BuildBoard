package models.jenkins

import models.{Builds, Build, Branch, Collection}


trait BuildsRepository {
  def getBuilds: Iterator[models.Build]

  def getBuilds(branch: models.Branch): Iterator[models.Build] = branch match {
    case models.Branch(name, _, pullRequest, _, _) =>
      val pullRequestId = pullRequest.map(p => p.prId)
      getBuilds.filter((build: models.Build) => build.branch == name || build.branch == s"origin/$name" || (pullRequestId.isDefined && build.branch == s"origin/pr/${pullRequestId.get}/merge"))
  }

  def getLastBuildsByBranch(branches: List[models.Branch]): Map[String, Option[models.Build]] = {
    val builds = getBuilds
    branches.map(b => {
      val pullRequestId = b.pullRequest.map(p => p.prId)
      val branchBuilds = builds.filter(build => build.branch == b.name || build.branch == s"origin/${b.name}" || (pullRequestId.isDefined && build.branch == s"origin/pr/${pullRequestId.get}/merge"))
      (s"origin/${b.name}", branchBuilds.toList.headOption)
    })
      .toMap
  }

  def getLastBuild(branch: models.Branch): Option[models.Build] = getBuilds(branch).toList.headOption

  def getBuild(branch: models.Branch, number: Int): Option[models.Build] = getBuilds(branch).find(_.number == number)
}

object JenkinsRepository extends BuildsRepository {
  val jenkinsAdapter = JenkinsAdapter

  def getBuilds = Builds.findAll()

  def forceBuild(action: models.BuildAction) = jenkinsAdapter.forceBuild(action)

  def toggleBuild(branch: models.Branch, number: Int): Option[models.Build] = getBuild(branch, number).map(build => {


    var newBuild = build.copy(toggled = !build.toggled)
//    println(newBuild)
    Builds.save(newBuild)

    newBuild
  })
}



