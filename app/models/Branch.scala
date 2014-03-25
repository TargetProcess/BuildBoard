package models

case class BranchInfo(name: String, url: String, pullRequest: Option[PullRequest] = None, entity: Option[Entity] = None, lastBuild: Option[BuildInfo] = None, activity: List[ActivityEntry] = Nil){
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

case class Branch(name: String, url: String, pullRequest: Option[PullRequest] = None, entity: Option[Entity] = None) {}

object BranchInfo {
  def serialize(branch: BranchInfo) = Some((branch.name, branch.url, branch.pullRequest, branch.entity, branch.lastBuild, branch.activity, branch.buildActions))
}

