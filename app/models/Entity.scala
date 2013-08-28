package models

import models.tp.TargetprocessApplication

case class Entity(id: Int, name: String, entityType: String, state: EntityState) {
  val url = TargetprocessApplication.getEntityUrl(id)
}

case class EntityState(
  id: Int,
  name: String,
  nextStates: Option[List[EntityState]] = None)
