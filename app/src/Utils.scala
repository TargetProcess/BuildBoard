package src

import play.api.Logger
import org.joda.time._


object Utils {
  def watch[T](title:String)(body: =>T):T = {
    val start = DateTime.now
    val result = body
    val delta = new Interval(start, DateTime.now).toDurationMillis
    Logger.info(s"$title: $delta")
    result
  }
}
