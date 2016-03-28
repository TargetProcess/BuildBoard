package models.configuration

import models.Branch


case class BuildConfig(cycles: List[CycleConfig], customCyclesAvailability: List[String], tests: Map[String, List[String]], unstableNodes: List[String])

case class BuildBoardConfig(branches: Map[String, String], build: BuildConfig, autoRerun: Map[String, Boolean], deploy: List[DeployConfig]) {
  def isApplicable(branchName: String, filter: List[String]): Boolean = {
    if (filter.contains("all"))
      true
    else {

      val branchCategories = branches
        .filter { case (name, regex) =>
          regex.r.findFirstIn(branchName).isDefined
        }.keys
        .toList

      val intersect: List[String] = filter.intersect(branchCategories)
      intersect.nonEmpty || (intersect.isEmpty && filter.contains("other"))
    }
  }
}

case class CycleConfig(name: String, branches: List[String], parameters: CycleParameters)

case class DeployConfig(name: String, channel: String, deployTo: String, branches: List[String])

case class CycleParameters(
                            isFull: Boolean,
                            includeUnstable: Boolean,
                            includeDb: Boolean,
                            includeComet: Boolean,
                            includeSlice: Boolean,
                            includePerfTests: Boolean,
                            includeMashupTests: Boolean,
                            buildFullPackage: Boolean,
                            casperTests: List[String],
                            karmaTests: List[String],
                            unitTests: List[String],
                            pythonFuncTests: List[String],
                            funcTests: List[String]
                          )
