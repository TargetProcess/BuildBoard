package components

import scala.util.Try

trait JenkinsRepositoryComponent {

  val jenkinsRepository: JenkinsRepository


  trait JenkinsRepository {
    def forceBuild(action: models.BuildAction): Try[String]
  }

}
