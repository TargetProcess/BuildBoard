package models.tp

import play.api.Play
import play.api.Play.current

object TargetprocessApplication {
  val tpUrl =  Play.configuration.getString("targetprocess.url").get

  def getEntityUrl(id: Int) = s"$tpUrl/entity/$id"

  def apiUri(root: String) = s"$tpUrl/api/v1/$root"

}
