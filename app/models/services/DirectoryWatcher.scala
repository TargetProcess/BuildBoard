/**
 * dirwatcher.scala
 *
 * Uses the Java 7 WatchEvent filesystem API from within Scala.
 * Adapted from:
 * http://download.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
 *
 * @author Chris Eberle <eberle1080@gmail.com>
 * @version 0.1
 */
package models.services

import java.io.IOException
import java.nio.file._
import java.nio.file.attribute._

import rx.lang.scala.{Subject, Observable}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.Breaks._
import ExecutionContext.Implicits.global

class DirectoryWatcher(val path: Path, val recursive: Boolean) {

  val watchService = path.getFileSystem.newWatchService()
  val keys = new mutable.HashMap[WatchKey, Path]
  var trace = false

  /**
   * Register a particular file or directory to be watched
   */
  def register(dir: Path): Unit = {
    val key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
      StandardWatchEventKinds.ENTRY_MODIFY,
      StandardWatchEventKinds.ENTRY_DELETE)

    if (trace) {
      val prev = keys.getOrElse(key, null)
      if (prev == null) {
        println("register: " + dir)
      } else {
        if (!dir.equals(prev)) {
          println("update: " + prev + " -> " + dir)
        }
      }
    }

    keys(key) = dir
  }

  /**
   * Makes it easier to walk a file tree
   */
  implicit def makeDirVisitor(f: (Path) => Unit) = new SimpleFileVisitor[Path] {
    override def preVisitDirectory(p: Path, attrs: BasicFileAttributes) = {
      f(p)
      FileVisitResult.CONTINUE
    }
  }

  /**
   * Recursively register directories
   */
  def registerAll(start: Path): Unit = {
    Files.walkFileTree(start, (f: Path) => {
      register(f)
    })
  }

  /**
   * The main directory watching thread
   */
  def run(): Observable[(Path, WatchEvent[_])] = {
    val observable = Subject[(Path, WatchEvent[_])]()

    val future = Future {
      try {
        if (recursive) {
          println("Scanning " + path + "...")
          registerAll(path)
          println("Done.")
        } else {
          register(path)
        }

        trace = true

        breakable {
          while (true) {
            val key = watchService.take()
            val dir = keys.getOrElse(key, null)
            if (dir != null) {
              key.pollEvents().asScala.foreach(event => {
                val kind = event.kind

                if (kind != StandardWatchEventKinds.OVERFLOW) {
                  val name = event.context().asInstanceOf[Path]
                  val child = dir.resolve(name)

                  observable.onNext((dir, event))

                  if (recursive && (kind == StandardWatchEventKinds.ENTRY_CREATE)) {
                    try {
                      if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        registerAll(child)
                      }
                    } catch {
                      case ioe: IOException => play.Logger.info("IOException: " + ioe)
                      case e: Exception => play.Logger.info("Exception: " + e)
                        break()
                    }
                  }
                }
              })
            } else {
              play.Logger.info("WatchKey not recognized!!")
            }

            if (!key.reset()) {
              keys.remove(key)
              if (keys.isEmpty) {
                break()
              }
            }
          }
        }
      } catch {
        case ie: InterruptedException => play.Logger.info("InterruptedException: " + ie)
        case ioe: IOException => play.Logger.info("IOException: " + ioe)
        case e: Exception => play.Logger.info("Exception: " + e)
      }
    }
    future.onFailure { case (x: Throwable) => observable.onError(x)}
    future.onSuccess { case (_) => observable.onCompleted()}

    observable
  }
}