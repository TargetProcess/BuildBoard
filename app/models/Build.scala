package models

trait Build {
  val number: Int
  val displayName: String
  val url: String
}

case class PullRequestBuild(number: Int, displayName: String, url: String) extends Build
case class NightBuild(number: Int, displayName: String, url: String) extends Build
