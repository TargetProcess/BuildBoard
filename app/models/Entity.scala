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
    case Some(x) => x.map(assignment => {
      state.role match {
        case Some(role) => assignment.copy(isResponsible = role == assignment.role)
        case None => assignment
      }

    })
    case None => Nil
  }

  def sortedAssignments = {
    val (developers, others) = assignments.sortBy(_.lastName).span(_.role == "Developer")
    developers ::: others
  }
}

case class EntityState(
                        id: Int,
                        name: String,
                        isFinalOpt: Option[Boolean],
                        role: Option[String],
                        nextStates: Option[List[EntityState]] = None) {
  val isFinal = isFinalOpt.getOrElse(false) || name=="Release Ready"
  val isReopen = name=="Reopen"
  val isQA = name == "Coded" || name=="Testing"
  val isTested = name=="Tested"
}

case class Assignment(userId:Int, role: String, avatar: String, firstName: String, lastName: String, isResponsible: Boolean = false) {
  val fullName = firstName + " " + lastName
}