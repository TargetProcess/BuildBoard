package models

trait Branch {
  val name: String
  var pullRequest: Option[PullRequest] = None
}

case class RegularBranch(name: String) extends Branch {
}

case class FeatureBranch(name: String, feature: String) extends Branch {
}

case class EntityBranch(name: String, entity:Option[Entity]) extends Branch {
}