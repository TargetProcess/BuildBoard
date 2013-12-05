package src

import com.github.nscala_time.time.Imports._

object Utils {
   def watch[T](title:String)(body: =>T):T = {
     val start = DateTime.now
     val result = body
     val delta = (start to DateTime.now).millis
     println(s"$title: $delta")
     result
   }
}
