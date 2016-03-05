package models.cycles

import models.buildActions.BuildParametersCategory
import play.api.Play
import play.api.Play.current

import scala.collection.JavaConverters._


case class CustomCycle(buildParametersCategory: List[BuildParametersCategory] = Nil) extends Cycle {

  override val name = "Custom"

  override val buildFullPackage = false
  override val includeUnstable: Boolean = false

  override val unitTests: String = getTestsByCategory(Cycle.unitTestsCategoryName)
  override val includeComet: Boolean = getBoolByCategory(Cycle.cometCategoryName)
  override val funcTests: String = getTestsByCategory(Cycle.funcTestsCategoryName)
  override val pythonFuncTests: String = getTestsByCategory(Cycle.pythonFuncTestsCategoryName)
  override val casperJsTests: String = getTestsByCategory(Cycle.casperCategoryName)
  override val karmaJsTests: String = getTestsByCategory(Cycle.karmaCategoryName)
  override val includeSlice: Boolean = getBoolByCategory(Cycle.sliceCategoryName)
  override val includeDb: Boolean = getBoolByCategory(Cycle.dbCategoryName)
  override val isFull: Boolean = getBoolByCategory(Cycle.cycleTypeCategoryName)
  override val includePerfTests: Boolean = getBoolByCategory(Cycle.perfCategoryName)

  def getBoolByCategory(categoryName: String): Boolean = {
    buildParametersCategory.find(x => x.name == categoryName).exists(x => x.parts.nonEmpty)
  }

  def getParamsByCategory(categoryName: String): Map[String, String] = {
    buildParametersCategory.filter(_.name == categoryName).flatMap(x => x.params).toMap
  }

  def getTestsByCategory(categoryName: String): String = {
    buildParametersCategory.find(x => x.name == categoryName).map(x => if (x.parts.isEmpty) "" else x.parts.mkString(" ")).getOrElse("All")
  }

  val getPossibleBuildParameters: List[BuildParametersCategory] = {
    List(
      BuildParametersCategory(Cycle.cycleTypeCategoryName, List("build full package")),
      BuildParametersCategory(Cycle.unitTestsCategoryName, getTests(Cycle.unitTestsCategoryName)),
      BuildParametersCategory(Cycle.funcTestsCategoryName,  getTests(Cycle.funcTestsCategoryName)),
      BuildParametersCategory(Cycle.pythonFuncTestsCategoryName,  getTests(Cycle.pythonFuncTestsCategoryName)),
      BuildParametersCategory(Cycle.cometCategoryName, List("Include")),
      BuildParametersCategory(Cycle.sliceCategoryName, List("Include")),
      BuildParametersCategory(Cycle.casperCategoryName, getTests(Cycle.casperCategoryName)),
      BuildParametersCategory(Cycle.karmaCategoryName, getTests(Cycle.karmaCategoryName)),
      BuildParametersCategory(Cycle.dbCategoryName, List("Include")),
      BuildParametersCategory(Cycle.perfCategoryName, List("Include"), Map(("PerfTestClass", ""), ("PerfTestMethod", "")))
    )
  }

  def getTests(testName: String): List[String] = {
    val config = Play.configuration.getConfig("build").get
    config.getStringList(testName).get.asScala.toList.distinct
  }


}
