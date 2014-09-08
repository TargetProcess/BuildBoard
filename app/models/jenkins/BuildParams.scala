package models.jenkins

import java.io.File

// branch => origin/develop | origin/pr/1044/merge | origin/feature/us76314
case class BuildParams(branch: String, parameters: Map[String, String])

object BuildParams extends FileApi {
  val branchName = "BRANCHNAME"
  val paramR = "([^:]*): (.*)".r

  def apply(file: File) = read(file).flatMap(str => {
    val lines = str.split('\n')
    val parameters: Map[String, String] = lines
      .flatMap {
      case paramR(key, value) => Some((key, value))
    }.toMap

    parameters.get(branchName)
      .map(n => new BuildParams(n, parameters.filter(_._1 != branchName)))
  })
}

