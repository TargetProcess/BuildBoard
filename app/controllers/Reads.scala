package controllers

import models.BuildParametersCategory
import play.api.libs.json.{Json, Reads}

object Reads {
  implicit val buildParametersCategoryReads = Json.reads[BuildParametersCategory]
  implicit val buildParameterCategoryReads: Reads[List[BuildParametersCategory]] = play.api.libs.json.Reads.list[BuildParametersCategory]
  implicit val reads = Json.reads[ForceBuildParameters]
}
