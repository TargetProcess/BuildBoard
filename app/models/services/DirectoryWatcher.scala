package models.services

import java.io.File

import com.sun.jna.platform.FileMonitor
import com.sun.jna.platform.win32.W32FileMonitor
import rx.lang.scala.Subject

class DirectoryWatcher(val path: File, val recursive: Boolean) {

  val observable = Subject[File]()

  val fm: W32FileMonitor = new W32FileMonitor

  try {
    fm.addWatch(path, FileMonitor.FILE_ANY, recursive)
    val listener: FileMonitor.FileListener = new FileMonitor.FileListener {
      def fileChanged(e: FileMonitor#FileEvent) {
        observable.onNext(e.getFile)
      }
    }
    fm.addFileListener(listener)
  }
  catch {
    case e: Throwable => observable.onError(e)
      fm.dispose()
  }
}