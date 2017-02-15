package models.cycles

import models.buildActions.BuildParametersCategory
import models.configuration.CycleParameters


case class Cycle(name: String, config: CycleParameters, buildParametersCategory: List[BuildParametersCategory], possibleBuildParameters: List[BuildParametersCategory]) {

  def getParamsByCategory(categoryName: String): Map[String, String] = {
    buildParametersCategory.filter(_.name == categoryName).flatMap(x => x.params).toMap
  }

  def toString(funcTests: List[String]): String = funcTests.mkString(" ")

  lazy val parameters = {
    val categorizedTests = config.tests.map { case (category, tests) => category.filter -> toString(tests) }.toList


    val otherParameters = List[(String, String)](
      CycleConstants.includePerfTestsKey -> config.includePerfTests.toString,
      CycleConstants.includeMashupTestsKey -> config.includeMashupTests.toString,
      "BuildFullPackage" -> config.buildFullPackage.toString,
      "INCLUDE_UNSTABLE" -> config.includeUnstable.toString,
      "Cycle" -> (if (config.isFull) "Full" else "Short"),
      "INCLUDE_COMET" -> config.includeComet.toString,
      "INCLUDE_SLICE" -> config.includeSlice.toString,
      "INCLUDE_DB" -> config.includeDb.toString,
      "INCLUDE_INTEGRATION" -> config.includeFuncIntegrationTests.toString)


    categorizedTests ++ otherParameters
  }
}