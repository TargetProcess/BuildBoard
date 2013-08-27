package models.github

import models.tp.Entity

trait Branch {
  val name: String
  var pullRequest: Option[PullRequest] = None
}

case class RegularBranch(val name: String) extends Branch {
}

case class FeatureBranch(override val name: String, val feature: String) extends Branch {
}

case class EntityBranch(override val name: String, entity: Entity) extends Branch {
}

object Branch {
  val EntityBranchPattern = "^(?i)feature/(us|bug|f)(\\d+).*".r
  val FeatureBranchPattern = "^(?i)feature/(\\w+)".r
  def create(name: String, pullRequest: Option[PullRequest]): Branch = {
    val branch = name match {
      case EntityBranchPattern(entityType: String, id: String) => {
        EntityBranch(name, Entity(id.toInt, entityType))
      }
      case FeatureBranchPattern(feature: String) => {
        FeatureBranch(name, feature)
      }
      case _ => RegularBranch(name)
    }
    branch.pullRequest = pullRequest

    branch
  }
}
