import models.services.CacheService
import play.api._
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter
import rx.lang.scala.Subscription

object Global extends WithFilters(new GzipFilter()) with GlobalSettings {
  var subscription: Subscription = null

  override def onStart(app: Application) {
    subscription = CacheService.start
  }

  override def onStop(app: Application) {
    subscription.unsubscribe()
  }
}