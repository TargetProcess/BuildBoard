package models

import com.github.nscala_time.time.Imports._

case class Build(status:String, url:String, timeStamp:DateTime, node: BuildNode)
case class BuildNode(status: String, statusUrl: String, artefactsUrl: String, parent: BuildNode = null, children: List[BuildNode] = List())
