package models.cycles

case class TestCategory(name:String, runName:String, filter:String, postfix:String)

object CycleConstants {
  val unitTestCategory = TestCategory("unitTests", "RunUnitTests", "UnitTestsFilter", "UnitTests")
  val funcTestCategory = TestCategory("funcTests", "RunFuncTests", "FuncTestsFilter", "FuncTests")
  val pythonTestCategory = TestCategory("pythonFuncTests", "RunFuncTestsPython", "PythonTestsFilter", "FuncTests")
  val karmaTestCategory = TestCategory("karmaTests", "RunKarmaJSTests", "KarmaJsTestsFilter", "FuncTests")
  val casperTestCategory = TestCategory("casperTests", "RunCasperJSTests", "CasperJsTestsFilter", "FuncTests")


  val sliceCategoryName = "SliceLoadTests"
  val cometCategoryName = "CometTests"
  val dbCategoryName = "DbTests"
  val cycleTypeCategoryName = "CycleType"
  val perfCategoryName = "PerfTests"
  val includePerfTestsKey = "INCLUDE_PERF"
  val includeMashupTestsKey = "RunMashupTests"


  val allTestCategories: Map[String, TestCategory] =
    List(unitTestCategory, funcTestCategory, pythonTestCategory, karmaTestCategory, casperTestCategory)
        .map(category=>(category.name, category))
    .toMap
}
