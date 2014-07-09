package models.jenkins

import models._
import java.io.File
import scala.xml.Node

trait Artifacts {
  this: FileApi =>

  protected val screenshot = "screenshot"
  protected val testNameRegex = """.*\.(\w+)\.(\w+)$""".r

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
