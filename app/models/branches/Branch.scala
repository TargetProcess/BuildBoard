package models.branches

import models.buildActions._
import models.{ActivityEntry, Build, Entity, PullRequest}

case class Branch(
                       name: String,
                       url: String,
                       pullRequest: Option[PullRequest] = None,
                       entity: Option[Entity] = None,
                       lastBuild: Option[Build] = None,
                       activity: List[ActivityEntry] = Nil
                       ) {

  val buildActions: List[BuildAction] = {

    val buildPackages = List(
      BranchBuildAction(name, BuildPackageOnly),
      BranchBuildAction(name, FullCycle)
    )

    val shortBuilds = name match {
      case BranchInfo.release(_) => Nil
      case BranchInfo.hotfix(_) => Nil
      case _ => List(BranchBuildAction(name, ShortCycle))
    }

    val pullRequestBuilds = pullRequest match {
      case Some(pr) if pr.status.isMergeable => List(
        PullRequestBuildAction(pr.prId, ShortCycle),
        PullRequestBuildAction(pr.prId, FullCycle)
      )
      case _ => Nil
    }

    buildPackages ++ shortBuilds ++ pullRequestBuilds
  }
}


object BranchInfo {
  val release = "^(?:origin/)?release/(.*)$".r
  val feature = "^(?:origin/)?feature/(.*)$".r
  val hotfix = "^(?:origin/)?hotfix/(.*)$".r
  val vs = "^(?:origin/)?vs/(.*)$".r
  val develop = "^(?:origin/)?develop$".r


  def serialize(branch: Branch) = Some((branch.name, branch.url, branch.pullRequest, branch.entity, branch.lastBuild, branch.activity, branch.buildActions))
}

