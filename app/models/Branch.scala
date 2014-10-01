package models

import models.buildActions._
import models.cycles.{ShortCycle, FullCycle, CustomCycle, PackageOnlyCycle}

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
      BranchBuildAction(name, PackageOnlyCycle),
      BranchBuildAction(name, FullCycle)
    )

    val buildBranches = name match {
      case BranchInfo.release(_) => Nil
      case BranchInfo.hotfix(_) => Nil
      case _ => List(BranchBuildAction(name, ShortCycle))
    }

    val (buildPullRequests, buildPullRequestCustom) =
      pullRequest match {
        case Some(pr) if pr.status.isMergeable => (
          List(
            PullRequestBuildAction(pr.prId, ShortCycle),
            PullRequestBuildAction(pr.prId, FullCycle)
          ),
          List(PullRequestCustomBuildAction(pr.prId, CustomCycle(List())))
          )
        case _ => (Nil, Nil)
      }

    val buildCustomBranch = name match {
      case BranchInfo.release(_) => Nil
      case BranchInfo.hotfix(_) => Nil
      case BranchInfo.develop() => Nil
      case _ => List(
        BranchCustomBuildAction(name, CustomCycle(List()))
      )
    }

    buildPackages ++ buildBranches ++ buildPullRequests ++ buildCustomBranch ++ buildPullRequestCustom
  }

}


/*  val buildActions: List[BuildAction] = {
    val l1 = List(
      BranchBuildAction(name, BuildPackageOnly),
      BranchBuildAction(name, FullCycle)
    )

    val l2 = name match {
      case BranchInfo.release(_) => Nil
      case BranchInfo.hotfix(_) => Nil
      case _ => List(BranchBuildAction(name, ShortCycle))
    }

    val (l3, l3Custom) =
      pullRequest match {
        case Some(pr) if pr.status.isMergeable => (
          List(
            PullRequestCustomBuildAction(pr.prId, CustomCycle(List())),
            PullRequestBuildAction(pr.prId, FullCycle)
          ),
          List(PullRequestBuildAction(pr.prId, ShortCycle)))
        case _ => (Nil, Nil)
      }

    val l4 = name match {
      case BranchInfo.release(_) => Nil
      case BranchInfo.hotfix(_) => Nil
      case BranchInfo.develop() => Nil
      case _ => List(
        BranchCustomBuildAction(name, CustomCycle(List()))
      )
    }

    l1 ++ l2 ++ l3 ++ l4 ++ l3Custom
  }
*/


object BranchInfo {
  val release = "^(?:origin/)?release/(.*)$".r
  val feature = "^(?:origin/)?feature/(.*)$".r
  val hotfix = "^(?:origin/)?hotfix/(.*)$".r
  val vs = "^(?:origin/)?vs/(.*)$".r
  val develop = "^(?:origin/)?develop$".r


  def serialize(branch: Branch) = Some((branch.name, branch.url, branch.pullRequest, branch.entity, branch.lastBuild, branch.activity))
}

