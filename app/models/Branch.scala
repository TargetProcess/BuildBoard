package models

case class Branch(
  name: String,
  pullRequest: Option[PullRequest],
  entity: Option[Entity])
