package models

import models.tp.TargetprocessApplication

case class Entity(val id: Int, val entityType: String, var state: String) {
  val url = TargetprocessApplication.getEntityUrl(id)
}
