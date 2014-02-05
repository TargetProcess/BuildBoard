package models.jenkins

import models._
import scala.Some


trait BuildsRepository {
  def getBuilds: List[models.Build]

  def getBranchPredicate(branch: models.Branch) = branch match {
    case models.Branch(name, _, pullRequest, _, _) =>
      val pullRequestId = pullRequest.map(_.prId)
      (branchName: String) => branchName == name || branchName == s"origin/$name" || (pullRequestId.isDefined && (branchName == s"origin/pr/${pullRequestId.get}/merge" || (branchName == s"pr/${pullRequestId.get}")))
  }

  def getBuilds(branch: models.Branch): List[models.Build] = {
    val predicate = getBranchPredicate(branch)
    val toggles = BuildToggles.findAll.filter(t => predicate(t.branch)).toList
    getBuilds.filter(b => predicate(b.branch))
      .toList
      .sortBy(-_.number)
      .map(b => if (toggles.exists(_.buildNumber == b.number)) b.copy(toggled = true) else b)
  }

  def getLastBuildsByBranch(branches: List[models.Branch]): Map[String, Option[models.Build]] = {
    val builds = getBuilds.toList
    val toggles = BuildToggles.findAll.toList
    branches.map(b => {
      val predicate = getBranchPredicate(b)
      val branchBuilds = builds.filter(build => predicate(build.branch))
      val lastBuild = branchBuilds
        .map(b => if (toggles.exists(t => predicate(t.branch) && t.buildNumber == b.number)) b.copy(toggled = true) else b)
        .sortBy(-_.number)
        .headOption
      (s"origin/${b.name}", lastBuild)
    })
      .toMap
  }

  def getLastBuild(branch: models.Branch): Option[models.Build] = getBuilds(branch).toList.headOption

  def getBuild(branch: models.Branch, number: Int): Option[models.Build] = getBuilds(branch).find(_.number == number)
}

class JenkinsRepository extends BuildsRepository {
  val jenkinsAdapter = JenkinsAdapter

  def getBuilds = jenkinsAdapter.getBuilds

  def forceBuild(action: models.BuildAction) = jenkinsAdapter.forceBuild(action)

  def getTestCasePackages(file: String) = jenkinsAdapter.getTestCasePackages(file)

  def getArtifact(file: String) = jenkinsAdapter.getArtifact(file)

  def toggleBuild(branch: models.Branch, number: Int): Option[models.Build] = getBuild(branch, number).map(build => {
    val predicate = getBranchPredicate(branch)
    BuildToggles.findAll.filter(t => predicate(t.branch) && t.buildNumber == number).toList.headOption match {
      case Some(toggle) =>
        BuildToggles.remove(toggle)
        build.copy(toggled = false)
      case None =>
        BuildToggles.save(BuildToggle(branch.name, number))
        build.copy(toggled = true)
    }
  })
}
class CachedJenkinsRepository extends JenkinsRepository {
  override def getBuilds: List[Build] = Builds.findAll().toList
}


