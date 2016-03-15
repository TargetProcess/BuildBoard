package models

import components.CycleBuilderComponent
import models.buildActions._
import models.cycles.Cycle

case class Branch(
                   name: String,
                   url: String,
                   pullRequest: Option[PullRequest] = None,
                   entity: Option[Entity] = None,
                   lastBuild: Option[Build] = None,
                   activity: List[ActivityEntry] = Nil
                 ) {

  def buildActions(cycleBuilderComponent: CycleBuilderComponent): List[BuildAction] = {
    val packageOnlyCycle: Cycle = cycleBuilderComponent.cycleBuilder.packageOnlyCycle
    val fullCycle: Cycle = cycleBuilderComponent.cycleBuilder.fullCycle
    val shortCycle: Cycle = cycleBuilderComponent.cycleBuilder.shortCycle

    val buildPackages = List(
      BranchBuildAction(name, packageOnlyCycle),
      BranchBuildAction(name, fullCycle)
    )

    val buildBranches = name match {
      case BranchInfo.release(_) => Nil
      case BranchInfo.hotfix(_) => Nil
      case _ => List(BranchBuildAction(name, shortCycle))
    }

    val (buildPullRequests, buildPullRequestCustom) =
      pullRequest match {
        case Some(pr) if pr.status.isMergeable => (
          List(
            PullRequestBuildAction(pr.prId, shortCycle),
            PullRequestBuildAction(pr.prId, fullCycle)
          ),
          List(PullRequestBuildAction(pr.prId, cycleBuilderComponent.cycleBuilder.emptyCustomCycle))
          )
        case _ => (Nil, Nil)
      }

    val buildCustomBranch = name match {
      case BranchInfo.release(_) => Nil
      case BranchInfo.hotfix(_) => Nil
      case BranchInfo.develop() => Nil
      case _ => List(
        BranchBuildAction(name, cycleBuilderComponent.cycleBuilder.emptyCustomCycle)
      )
    }

    buildPackages ++
      buildBranches ++
      buildPullRequests ++
      buildCustomBranch ++
      buildPullRequestCustom ++
      List(TransifexBuildAction(name))
  }

}

object BranchInfo {
  val release = "^(?:origin/)?release/(.*)$".r
  val feature = "^(?:origin/)?feature/(.*)$".r
  val hotfix = "^(?:origin/)?hotfix/(.*)$".r
  val vs = "^(?:origin/)?vs/(.*)$".r
  val develop = "^(?:origin/)?develop$".r
  val pr = "^(?:origin/)?/pr/(.*?)/merge".r

  def serialize(branch: Branch) = Some((branch.name, branch.url, branch.pullRequest, branch.entity, branch.lastBuild, branch.activity))
}

