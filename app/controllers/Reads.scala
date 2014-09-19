package controllers

import controllers.Jenkins.ForceBuildParameters
import play.api.libs.json.{Json}

object Reads {
//  implicit val buildParametersCategoryReads = Json.reads[BuildParametersCategory]
  //implicit val buildParameterCategoryReads: play.api.libs.json.Reads[List[BuildParametersCategory]] = play.api.libs.json.Reads.list[BuildParametersCategory]
  implicit val reads = Json.reads[ForceBuildParameters]
}
