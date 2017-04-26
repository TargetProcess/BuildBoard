package models.cycles

case class TestCategory(name: String, runName: String, filter: String, postfix: String)

object CycleConstants {
  val sliceCategoryName = "SliceLoadTests"
  val cometCategoryName = "CometTests"
  val dbCategoryName = "DbTests"
  val cycleTypeCategoryName = "CycleType"
  val perfCategoryName = "PerfTests"
  val funcIntegrationTestsCategoryName = "RunFuncIntegrationTests"
  val includePerfTestsKey = "INCLUDE_PERF"
  val includeMashupTestsKey = "RunMashupTests"


  val allTestCategories: Map[String, TestCategory] =
    List(
      TestCategory("unitTests", "RunUnitTests", "UnitTestsFilter", "UnitTests"),
      TestCategory("funcTests", "RunFuncTests", "FuncTestsFilter", "FuncTests"),
      TestCategory("pythonFuncTests", "RunFuncTestsPython", "PythonTestsFilter", "FuncTests"),
      TestCategory("karmaTests", "RunKarmaJSTests", "KarmaJsTestsFilter", "FuncTests"),
      TestCategory("casperTests", "RunCasperJSTests", "CasperJsTestsFilter", "FuncTests")
    )
      .map(category => (category.name, category))
      .toMap
}
