package models.buildWatcher

import components.BuildWatcherComponent
import rx.lang.scala.{Observable, Subject}

object Notification {
  val subject: Subject[String] = Subject()

}


trait NotificationBuildWatcherComponentImpl extends BuildWatcherComponent {
  override val buildWatcher = new BuildWatcher {


    override def forceUpdate(buildName: String): Unit = {
      play.Logger.info(s"forceBuild: $buildName")
      Notification.subject.onNext(buildName)
    }

    override def start: Observable[String] = Notification.subject
  }
}
