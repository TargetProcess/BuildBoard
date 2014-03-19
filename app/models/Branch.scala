package models

case class Branch(
                   name: String,
                   url: String,
                   pullRequest: Option[PullRequest] = None,
                   entity: Option[Entity] = None,
                   lastBuild: Option[BuildInfo] = None,
                   activity: List[ActivityEntry] = Nil
                   ) {
  def buildActions: List[BuildAction] = {
    List(
      BranchBuildAction(name, BuildPackageOnly),
      BranchBuildAction(name, FullCycle),
      BranchBuildAction(name, ShortCycle)
    ) ++ pullRequest.map(pr => List(
      PullRequestBuildAction(pr.prId, FullCycle),
      PullRequestBuildAction(pr.prId, ShortCycle)
    )).getOrElse(Nil)
   }
}

object Branch {
  def serialize(branch: Branch) = Some((branch.name, branch.url, branch.pullRequest, branch.entity, branch.lastBuild, branch.activity, branch.buildActions))
}

