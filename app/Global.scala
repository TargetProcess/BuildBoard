import buildboard2.model.services.BuildBoard2CacheService
import models.services.CacheService
import play.api._
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter
import rx.lang.scala.Subscription

object Global extends WithFilters(new GzipFilter()) with GlobalSettings {
  var subscription: Subscription = null
  var bb2Subscription: Subscription = null

  override def onStart(app: Application) {
    subscription = CacheService.start
    bb2Subscription = BuildBoard2CacheService.start()
  }

  override def onStop(app: Application) {
    subscription.unsubscribe()
    bb2Subscription.unsubscribe()
  }
}