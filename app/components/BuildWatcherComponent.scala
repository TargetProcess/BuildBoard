package components

import rx.lang.scala.Observable

trait BuildWatcherComponent {
  val buildWatcher: BuildWatcher

  trait BuildWatcher {
    def start:Observable[String]
    def forceUpdate(build:String)
  }

}
