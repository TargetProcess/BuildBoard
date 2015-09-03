package components

import models.teams.Team

trait ConfigComponent {
  val config: ConfigService

  trait ConfigService {
    val deployDirectoryRoot:String
    val jenkinsDataPath:String
    val jenkinsUrl:String
    val teams:List[Team]

    val unstableNodes:List[String]
  }

}
