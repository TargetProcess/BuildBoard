package models.cycles

object Cycle {
  val unitTestsCategoryName = "unitTests"
  val funcTestsCategoryName = "funcTests"
  val pythonFuncTestsCategoryName = "pythonFuncTests"
  val sliceCategoryName = "SliceLoadTests"
  val cometCategoryName = "CometTests"
  val casperCategoryName = "casperTests"
  val karmaCategoryName = "karmaTests"
  val dbCategoryName = "DbTests"
  val cycleTypeCategoryName = "CycleType"
  val perfCategoryName = "PerfTests"

  val includePerfTestsKey = "INCLUDE_PERF"
  val includeFuncTestsKey = "FuncTestsFilter"
  val includePythonTestsKey = "PythonTestsFilter"
  val includeCasperJsTestsKey = "CasperJsTestsFilter"
  val includeKarmaJsTestsFilter = "KarmaJsTestsFilter"
}

trait Cycle {
  val name: String

  val includeUnstable: Boolean
  val buildFullPackage: Boolean
  val unitTests: String
  val funcTests: String
  val casperJsTests: String
  val karmaJsTests: String
  val pythonFuncTests: String
  val includeComet: Boolean
  val includeSlice: Boolean
  val includeDb: Boolean
  val isFull: Boolean
  val includePerfTests: Boolean

  lazy val parameters = {
    List("UnitTestsFilter" -> unitTests,
      Cycle.includeFuncTestsKey -> funcTests,
      Cycle.includePythonTestsKey -> pythonFuncTests,
      Cycle.includePerfTestsKey -> includePerfTests.toString,
      Cycle.includeCasperJsTestsKey -> casperJsTests,
      Cycle.includeKarmaJsTestsFilter -> karmaJsTests,
      "BuildFullPackage" -> buildFullPackage.toString,
      "INCLUDE_UNSTABLE" -> includeUnstable.toString,
      "Cycle" -> (if (isFull) "Full" else "Short"),
      "INCLUDE_COMET" -> includeComet.toString,
      "INCLUDE_SLICE" -> includeSlice.toString,
      "INCLUDE_DB" -> includeDb.toString)
  }

}