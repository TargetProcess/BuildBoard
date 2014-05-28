package models

import models.tp.TargetprocessApplication

case class Entity(
                   id: Int,
                   name: String,
                   entityType: String,
                   state: EntityState,
                   assignments: List[Assignment] = Nil,
                   url: String) {
}

object Entity {
  def create(id: Int,
             name: String,
             entityType: String,
             state: EntityState,
             assignmentsOpt: Option[List[Assignment]]) =
    new Entity(id, name, entityType, state, assignmentsOpt match {
      case Some(x) => x.map(assignment => {
        state.role match {
          case Some(role) => assignment.copy(isResponsible = role == assignment.role)
          case None => assignment
        }

      })
      case None => Nil
    },
      TargetprocessApplication.getEntityUrl(id))

}

case class EntityState(id: Int,
                       name: String,
                       isFinal: Boolean,
                       role: Option[String] = None,
                       nextStates: List[EntityState] = Nil)

object EntityState {
  def create(id: Int,
             name: String,
             isFinalOpt: Option[Boolean],
             role: Option[String],
             nextStates: Option[List[EntityState]] = None) =
    new EntityState(
      id,
      name,
      isFinalOpt.getOrElse(false) || name == "Release Ready",
      role,
      nextStates.getOrElse(Nil))

}

case class Assignment(userId: Int, role: String, avatar: String, firstName: String, lastName: String, isResponsible: Boolean = false) {
  val fullName = firstName + " " + lastName
}