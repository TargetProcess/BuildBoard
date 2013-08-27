package models

case class Entity(val id: Int, val entityType: String) {
  val link = ""
  val url = TargetprocessApplication.getEntityUrl(id)
}
