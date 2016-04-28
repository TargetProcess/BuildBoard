package models.cycles

object CycleConstants {
  val unitTestsCategoryName = "unitTests"
  val unitTestsRunName = "RunUnitTests"

  val funcTestsCategoryName = "funcTests"
  val funcTestsRunName = "RunFuncTests"

  val pythonFuncTestsRunName = "RunFuncTestsPython"
  val pythonFuncTestsCategoryName = "pythonFuncTests"

  val karmaCategoryName = "karmaTests"
  val karmaRunName = "RunKarmaJSTests"

  val sliceCategoryName = "SliceLoadTests"
  val cometCategoryName = "CometTests"
  val casperCategoryName = "casperTests"
  val dbCategoryName = "DbTests"
  val cycleTypeCategoryName = "CycleType"
  val perfCategoryName = "PerfTests"

  val includePerfTestsKey = "INCLUDE_PERF"
  val includeMashupTestsKey = "RunMashupTests"
  val includeFuncTestsKey = "FuncTestsFilter"
  val includePythonTestsKey = "PythonTestsFilter"
  val includeCasperJsTestsKey = "CasperJsTestsFilter"
  val includeKarmaJsTestsFilter = "KarmaJsTestsFilter"


  val partitionedTests = Map(
    CycleConstants.funcTestsCategoryName -> CycleConstants.funcTestsRunName,
    CycleConstants.unitTestsCategoryName -> CycleConstants.unitTestsRunName,
    CycleConstants.pythonFuncTestsCategoryName -> CycleConstants.pythonFuncTestsRunName,
    CycleConstants.karmaCategoryName -> CycleConstants.karmaRunName
  )

}
