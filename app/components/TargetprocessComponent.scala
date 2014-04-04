package components

import models.{TpUser, Entity, EntityState}
import scala.util.Try

trait TargetprocessComponent {
  def entityRepository: EntityRepository

  trait EntityRepository {
    def changeEntityState(entityId: Int, stateId: Int): EntityState
    def getAssignables(ids: List[Int]): List[Entity]
    def getLoggedUser: (TpUser, String)
  }
}
