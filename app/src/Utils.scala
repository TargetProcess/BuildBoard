package src

import com.github.nscala_time.time.Imports._
import play.api.Logger

object Utils {
   def watch[T](title:String)(body: =>T):T = {
     val start = DateTime.now
     val result = body
     val delta = (start to DateTime.now).millis
     Logger.info(s"$title: $delta")
     result
   }
}
