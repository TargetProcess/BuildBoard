package models.jenkins

import scala.util.Try
import java.io.File
import scala.io.{BufferedSource, Source}
import play.api.Play
import play.api.Play.current

trait FileApi {

  val directory = Play.configuration.getString("jenkins.data.path").get

  def read(f: File): Option[String] = Try {
    val file: BufferedSource = Source.fromFile(f)
    val result = file.mkString
    file.close()

    result
  }.toOption
}
