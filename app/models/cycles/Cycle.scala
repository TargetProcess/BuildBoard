package models.cycles

object Cycle {
  val unitTestsCategoryName = "unitTests"
  val funcTestsCategoryName = "funcTests"
  val pythonFuncTestsCategoryName = "pythonFuncTests"
  val sliceCategoryName = "SliceLoadTests"
  val cometCategoryName = "CometTests"
  val casperCategoryName = "CasperTests"
  val dbCategoryName = "DbTests"
  val cycleTypeCategoryName = "CycleType"
  val perfCategoryName = "PerfTests"

  val includePerfTestsKey = "INCLUDE_PERF"
  val includeFuncTestsKey = "IncludeFuncTests"
}

trait Cycle {
  val name: String

  val includeUnstable: Boolean
  val buildFullPackage: Boolean
  val unitTests: String
  val funcTests: String
  val pythonFuncTests: String
  val includeComet: Boolean
  val includeSlice: Boolean
  val includeCasper: Boolean
  val includeDb: Boolean
  val isFull: Boolean
  val includePerfTests: Boolean

  lazy val parameters = {
    List("IncludeUnitTests" -> unitTests,
      Cycle.includeFuncTestsKey -> funcTests,
      Cycle.includePerfTestsKey -> includePerfTests.toString,
      "BuildFullPackage" -> buildFullPackage.toString,
      "INCLUDE_UNSTABLE" -> includeUnstable.toString,
      "Cycle" -> (if (isFull) "Full" else "Short"),
      "INCLUDE_COMET" -> includeComet.toString,
      "INCLUDE_SLICE" -> includeSlice.toString,
      "INCLUDE_CASPER" -> includeCasper.toString,
      "INCLUDE_DB" -> includeDb.toString)
  }

}