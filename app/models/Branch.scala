package models

trait Branch {
  val name: String
}

case class RegularBranch(val name: String) extends Branch {
}

case class FeatureBranch(override val name: String, val feature: String) extends Branch {
}

case class EntityBranch(override val name: String, entity: Entity) extends Branch {
}

object Branch {
  val EntityBranchPattern = "^(feature|hotfix|release)/(us|bug|f)(\\d+)$".r
  val FeatureBranchPattern = "^feature/(\\w+)".r
  def create(name: String): Branch = {
    name match {
      case EntityBranchPattern(branchType: String, entityType: String, id: String) => {
        EntityBranch(name, Entity(id.toInt, entityType))
      }
      case FeatureBranchPattern(feature: String) => {
        FeatureBranch(name, feature)
      }
      case _ => RegularBranch(name)
    }
  }
}
