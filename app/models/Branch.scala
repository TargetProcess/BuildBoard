package models

case class Branch(
                   name: String,
                   pullRequest: Option[PullRequest],
                   entity: Option[Entity],
                   builds: Iterable[Build]) {
}


case class EntityBranch(name: String,
                        pullRequest: Option[PullRequest],
                        entity: Entity)

object EntityBranch {
  implicit def branchToEntityBranch(b: Branch): EntityBranch = EntityBranch(b.name, b.pullRequest, b.entity.get)

  implicit def branchesToEntityBranches(b: List[Branch]) = b.filter(_.entity.isDefined).map(branchToEntityBranch)
}

