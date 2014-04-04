package models.jenkins

import java.io.File
import models.Artifact
import scala.util.Try
import scalaj.http.Http
import models._
import java.io.File
import scala.io.{BufferedSource, Source}
import scala.xml.{Node, XML}
import models.BuildNode
import scala.Some
import models.TestCase
import models.Build
import models.TestCasePackage
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._
import play.api.Play
import play.api.Play.current

trait Artifacts {

  protected val screenshot = "screenshot"
  protected val testNameRegex = """.*\.(\w+)\.(\w+)$""".r
  protected val screenshotQualifiedFileNameRegex = """.*\\(\w+)\.(\w+)[-].*$""".r
  protected val screenshotFileNameRegex = """.*\\(\w+)[-].*$""".r

  def getArtifacts(contents: List[File]): List[Artifact] = {
    def getArtifactsInner(file: File, filter: File => Boolean, artifactName: String): List[Artifact] = file.listFiles
      .filter(filter)
      .map(_.getPath.substring(directory.length + 1))
      .map(Artifact(artifactName, _))
      .toList

    contents.map(file => file.getName match {
      case ".TestResults" => getArtifactsInner(file, f => f.getName.endsWith(".xml"), "testResults")
      case ".Logs" => getArtifactsInner(file, f => f.getName.startsWith("SessionLogs"), "logs")
      case ".Screenshots" => getArtifactsInner(file, _ => true, screenshot)
      case _ => List()
    })
      .flatten
  }

  def getArtifact(file: String): File = new File(directory, file)

  def getAttribute(n: Node, key: String): Option[String] = n.attribute(key).map(_.headOption.map(_.text)).flatten
}
