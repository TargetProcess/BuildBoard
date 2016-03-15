package components

import models.teams.Team

import scala.concurrent.duration.FiniteDuration

trait ConfigComponent {
  val config: ConfigService

  trait ConfigService {

    val deployDirectoryRoot:String
    val jenkinsDataPath:String
    val jenkinsInterval:FiniteDuration


    val jenkinsUrl:String
    val teams:List[Team]

    val unstableNodes:List[String]
  }

}


