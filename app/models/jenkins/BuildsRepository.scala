package models.jenkins

import models._


trait BuildsRepository {
  def getBranchPredicate(branch: Branch) = branch match {
    case Branch(name, _, pullRequest, _, _) =>
      val pullRequestId = pullRequest.map(_.prId)
      (branchName: String) => {
        println(s"comparing $branchName with $name")
        branchName == name || branchName == s"origin/$name" || (pullRequestId.isDefined && (branchName == s"origin/pr/${pullRequestId.get}/merge" || (branchName == s"pr/${pullRequestId.get}")))
      }
  }

  def getBuilds(branch: Branch): List[BuildInfo]

  def getLastBuildsByBranch(branches: List[Branch]): Map[String, Option[BuildInfo]] = branches
    .map(b => (b.name, getLastBuild(b)))
    .toMap

  def getLastBuild(branch: Branch): Option[BuildInfo] = getBuilds(branch).toList.headOption
}




