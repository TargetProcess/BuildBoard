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
    cycleBuilderComponent.cycleBuilder.buildActions(this)
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

