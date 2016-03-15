package models.cycles

import models.buildActions.BuildParametersCategory




case class Cycle(
                  name: String,
                  includeUnstable: Boolean,
                  buildFullPackage: Boolean,
                  unitTests: String,
                  funcTests: String,
                  casperJsTests: String,
                  karmaJsTests: String,
                  pythonFuncTests: String,
                  includeComet: Boolean,
                  includeSlice: Boolean,
                  includeDb: Boolean,
                  isFull: Boolean,
                  includePerfTests: Boolean,
                  buildParametersCategory: List[BuildParametersCategory] = Nil,
                  possibleBuildParameters: List[BuildParametersCategory] = Nil
                ) {


  def getBoolByCategory(categoryName: String): Boolean = {
    buildParametersCategory.find(x => x.name == categoryName).exists(x => x.parts.nonEmpty)
  }

  def getParamsByCategory(categoryName: String): Map[String, String] = {
    buildParametersCategory.filter(_.name == categoryName).flatMap(x => x.params).toMap
  }

  def getTestsByCategory(categoryName: String): String = {
    buildParametersCategory.find(x => x.name == categoryName).map(x => if (x.parts.isEmpty) "" else "\"" + x.parts.mkString(" ") + "\"").getOrElse("All")
  }

  lazy val parameters = {
    List("UnitTestsFilter" -> unitTests,
      CycleConstants.includeFuncTestsKey -> funcTests,
      CycleConstants.includePythonTestsKey -> pythonFuncTests,
      CycleConstants.includePerfTestsKey -> includePerfTests.toString,
      CycleConstants.includeCasperJsTestsKey -> casperJsTests,
      CycleConstants.includeKarmaJsTestsFilter -> karmaJsTests,
      "BuildFullPackage" -> buildFullPackage.toString,
      "INCLUDE_UNSTABLE" -> includeUnstable.toString,
      "Cycle" -> (if (isFull) "Full" else "Short"),
      "INCLUDE_COMET" -> includeComet.toString,
      "INCLUDE_SLICE" -> includeSlice.toString,
      "INCLUDE_DB" -> includeDb.toString)
  }


}