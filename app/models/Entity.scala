package models

import models.tp.TargetprocessApplication

case class Entity(
  id: Int,
  name: String,
  entityType: String,
  state: EntityState,
  assignmentsOpt: Option[List[Assignment]]) {
  val url = TargetprocessApplication.getEntityUrl(id)
  val assignments = assignmentsOpt match {
    case Some(x) => x
    case None => Nil
  }
}

case class EntityState(
  id: Int,
  name: String,
  isFinalOpt: Option[Boolean],
  nextStates: Option[List[EntityState]] = None) {
  val isFinal = isFinalOpt.getOrElse(false)
}

case class Assignment(role: String, avatar: String, firstName: String, lastName: String) {
  val fullName = firstName + lastName
}