import models.github.GithubService
import play.api._
import scala.concurrent.duration._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    GithubService.start(10 seconds)
  }
}