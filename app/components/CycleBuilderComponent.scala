package components

import models.Branch
import models.buildActions.{BuildAction, BuildParametersCategory}
import models.cycles.Cycle

trait CycleBuilderComponent {
  val cycleBuilder: CycleBuilder

  trait CycleBuilder {
    def find(cycleName: String):Option[Cycle]

    def buildActions(branch: Branch): List[BuildAction]

    val emptyCustomCycle = customCycle(Nil)
    def customCycle(buildParametersCategory: List[BuildParametersCategory]): Cycle
  }

}
