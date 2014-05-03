package models.jenkins

import java.io.File

// branch => origin/develop | origin/pr/1044/merge | origin/feature/us76314
case class BuildParams(branch: String, parameters: Map[String, String])

object BuildParams extends FileApi {
  val branchNameR = "BRANCHNAME: (.*)".r
  val paramR = "([^:]*): (.*)".r

  def apply(file: File) = read(file).flatMap(str => {
    val lines = str.split('\n')

    val name = lines(0) match {
      case branchNameR(n) => Some(n)
      case _ => None
    }


    val parameters: Map[String, String] = lines.drop(1)
      .flatMap {
      case paramR(key, value) => Some((key, value))
    }.toMap

    name.map(n => new BuildParams(n, parameters))
  })
}

