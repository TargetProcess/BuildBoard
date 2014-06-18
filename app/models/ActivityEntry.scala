package models

import org.joda.time.DateTime
import com.novus.salat.annotations.raw.Salat

@Salat
trait ActivityEntry {
  val activityType: String
  val timestamp: DateTime
}



