import models.services.CacheService
import play.api._
import rx.lang.scala.Subscription

object Global extends GlobalSettings {
  var subscription: Subscription = null

  override def onStart(app: Application) {
    subscription = CacheService.start
  }

  override def onStop(app: Application) {
    subscription.unsubscribe()
  }
}