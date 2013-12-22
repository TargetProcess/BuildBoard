import models.github.GithubService
import play.api._
import rx.lang.scala.Subscription
import scala.concurrent.duration._

object Global extends GlobalSettings {
  var subscription:Subscription = null
  override def onStart(app: Application) {
    subscription = GithubService.start(10 seconds)
  }
  override def onStop(app:Application){
    subscription.unsubscribe()
  }
}