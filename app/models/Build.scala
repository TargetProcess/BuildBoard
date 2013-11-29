package models

import com.github.nscala_time.time.Imports._

case class Build(branch: String, status:String, url:String, timeStamp:DateTime, node: BuildNode)
case class BuildNode(status: String, statusUrl: String, artefactsUrl: String, children: List[BuildNode] = Nil)
