package models

import com.github.nscala_time.time.Imports._

case class Build(number: Int, branch: String, status:Option[String], url:String, timeStamp:DateTime, node: BuildNode)
case class BuildNode(number: Int, name: String, status: Option[String], statusUrl: String, artefactsUrl: String, children: Option[List[BuildNode]] = None)
