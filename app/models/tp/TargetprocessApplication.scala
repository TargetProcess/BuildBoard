package models.tp

object TargetprocessApplication {
  val tpUrl = "http://plan.tpondemand.com"
  def getEntityUrl(id: Int) = s"$tpUrl/entity/$id"
}
