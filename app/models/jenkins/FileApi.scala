package models.jenkins

import java.io.{File,FileInputStream,FileOutputStream}

import play.api.Play
import play.api.Play.current

import scala.io.{BufferedSource, Source}
import scala.util.Try

trait FileApi {

  val directory = Play.configuration.getString("jenkins.data.path").get
  val deployDirectory = Play.configuration.getString("jenkins.data.deployPath").get

  def read(f: File): Option[String] = Try {
    val file: BufferedSource = Source.fromFile(f)
    val result = file.mkString
    file.close()

    result
  }.toOption

  def readAsMap(f: File): Option[Map[Int, String]] = read(f)
    .map(
      _.split('\n')
        .map(_.replace("\r", "").trim)
        .zipWithIndex.map(_.swap).toMap
    )

  def copyFile(fromFile: File, toFolder: File) = {
    val src = fromFile
    val dest = new File(toFolder.getPath() + "/" + fromFile.getName())
    new FileOutputStream(dest) getChannel() transferFrom(
    new FileInputStream(src) getChannel, 0, Long.MaxValue )
  }



}
