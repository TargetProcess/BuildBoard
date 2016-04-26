package src

import play.api.Logger
import org.joda.time._

import scala.concurrent.duration.FiniteDuration


object Utils {
  def watch[T](title: String)(body: => T): T = {
    val start: DateTime = DateTime.now
    Logger.info(s"start: $title")
    val result = body
    val delta = new Interval(start, DateTime.now).toDurationMillis
    Logger.info(s"stop:  $title (took $delta ms)")
    result
  }

  def dump[T](title:String)(body: => T): T = {
    val result = body
    Logger.info(s"$title: $result")
    result
  }
}
