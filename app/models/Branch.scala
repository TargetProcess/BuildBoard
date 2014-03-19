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
    val name = this.name
    val pullRequest = this.pullRequest

    List(
      BranchBuildAction(name, BuildPackageOnly),
      BranchBuildAction(name, FullCycle),
      BranchBuildAction(name, ShortCycle)
    ) ++ (pullRequest match {
      case Some(pr) => List(
        PullRequestBuildAction(pr.prId, FullCycle),
        PullRequestBuildAction(pr.prId, ShortCycle)
      )
      case None => Nil
    })
  }
}

object Branch {


  def serialize(branch: Branch) = Some((branch.name, branch.url, branch.pullRequest, branch.entity, branch.lastBuild, branch.activity, branch.buildActions))
}

