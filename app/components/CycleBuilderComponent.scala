package components

import models.buildActions.BuildParametersCategory
import models.cycles.Cycle

trait CycleBuilderComponent {
  val cycleBuilder: CycleBuilder

  trait CycleBuilder {
    def find(cycleName: String) = getAllCycles.find(_.name == cycleName)

    val fullCycle = cycle("Full")
    val shortCycle = cycle("Short")
    val packageOnlyCycle = cycle("Package only")
    val emptyCustomCycle = customCycle(Nil)

    val getAllCycles = List(fullCycle, shortCycle, packageOnlyCycle)

    def cycle(name: String): Cycle
    def customCycle(buildParametersCategory: List[BuildParametersCategory]): Cycle
  }

}
