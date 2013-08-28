package models.tp

object TargetprocessApplication {
  val tpUrl = "http://plan.tpondemand.com"
  def getEntityUrl(id: Int) = s"$tpUrl/entity/$id"
  
  def apiUri(root:String) = s"$tpUrl/api/v1/$root"
}
