package models

case class BranchInfo(name: String, url: String, pullRequest: Option[PullRequest] = None, entity: Option[Entity] = None, lastBuild: Option[BuildInfo] = None, activity: List[ActivityEntry] = Nil) {
  val buildActions: List[BuildAction] = {
    val l1 = List(
      BranchBuildAction(name, BuildPackageOnly),
      BranchBuildAction(name, FullCycle)
    )

    val l2 = if (!name.startsWith("release") && !name.startsWith("hotfix")) List(BranchBuildAction(name, ShortCycle)) else Nil

    val l3 =
      pullRequest match {
        case Some(pr) if pr.status.isMergeable => List(
          PullRequestBuildAction(pr.prId, FullCycle),
          PullRequestBuildAction(pr.prId, ShortCycle))
        case _ => Nil
      }

    l1 ++ l2 ++ l3
  }
}


case class Branch(name: String, url: String, pullRequest: Option[PullRequest] = None, entity: Option[Entity] = None) {}

object BranchInfo {
  val release = "^(?:origin/)?release/(.*)$".r
  val feature = "^(?:origin/)?feature/(.*)$".r
  val hotfix = "^(?:origin/)?hotfix/(.*)$".r
  val vs = "^(?:origin/)?vs/(.*)$".r
  val develop = "^(?:origin/)?develop$".r



  def serialize(branch: BranchInfo) = Some((branch.name, branch.url, branch.pullRequest, branch.entity, branch.lastBuild, branch.activity, branch.buildActions))
}

