package models

case class BranchInfo(name: String, url: String, pullRequest: Option[PullRequest] = None, entity: Option[Entity] = None, lastBuild: Option[BuildInfo] = None, activity: List[ActivityEntry] = Nil) {
  val buildActions: List[BuildAction] = {
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
}

case class Branch(
                   name: String,
                   url: String,
                   pullRequest: Option[PullRequest] = None,
                   entity: Option[Entity] = None
                   )

object BranchInfo {
  val release = "^(?:origin/)?release/(.*)$".r
  val feature = "^(?:origin/)?feature/(.*)$".r
  val hotfix = "^(?:origin/)?hotfix/(.*)$".r
  val vs = "^(?:origin/)?vs/(.*)$".r
  val develop = "^(?:origin/)?develop$".r


  def serialize(branch: BranchInfo) = Some((branch.name, branch.url, branch.pullRequest, branch.entity, branch.lastBuild, branch.activity, branch.buildActions))
}

