package models.buildWatcher

import java.io.File

import com.sun.jna.platform.FileMonitor
import com.sun.jna.platform.win32.W32FileMonitor
import components.{BuildWatcherComponent, ConfigComponent}
import rx.lang.scala.{Observable, Subject}

trait FileBuildWatcherComponentImpl extends BuildWatcherComponent {
  this: FileBuildWatcherComponentImpl
    with ConfigComponent
  =>


  val buildWatcher: BuildWatcher = new BuildWatcher {

    val watcher = new DirectoryWatcher(new File(config.jenkinsDataPath), true)

    override def start: Observable[String] = {
      watcher.start()
      watcher.observable
    }

    override def forceUpdate(build: String): Unit = {
      watcher.observable.onNext(build)
    }
  }


  class DirectoryWatcher(val path: File, val recursive: Boolean) {
    val observable: Subject[String] = Subject[String]()
    val fm: W32FileMonitor = new W32FileMonitor

    def start() = {
      try {
        fm.addWatch(path, FileMonitor.FILE_ANY, recursive)
        val listener: FileMonitor.FileListener = new FileMonitor.FileListener {
          def fileChanged(e: FileMonitor#FileEvent) {

            val file = e.getFile
            val directoryName = path.toPath.relativize(file.toPath).subpath(0, 1)

            observable.onNext(directoryName.toString)
          }
        }
        fm.addFileListener(listener)
      }
      catch {
        case e: Throwable => observable.onError(e)
          fm.dispose()
      }
    }
  }

}