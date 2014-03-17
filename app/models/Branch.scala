package models

case class Branch(name:String, url: String, pullRequest: Option[PullRequest] = None, entity: Option[Entity] = None, activity: List[ActivityEntry] = Nil)
