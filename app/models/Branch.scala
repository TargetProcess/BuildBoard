package models

case class Branch(name:String, url: String, pullRequest: Option[PullRequest] = None, entity: Option[Entity] = None, lastBuild: Option[BuildInfo] = None, activity: List[ActivityEntry] = Nil, buildActions: List[BuildAction] = Nil)
