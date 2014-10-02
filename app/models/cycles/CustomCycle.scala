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
  override val includeSlice: Boolean = getBoolByCategory(Cycle.sliceCategoryName)
  override val includeCasper: Boolean = getBoolByCategory(Cycle.casperCategoryName)
  override val includeDb: Boolean = getBoolByCategory(Cycle.dbCategoryName)
  override val isFull: Boolean = getBoolByCategory(Cycle.cycleTypeCategoryName)

  def getBoolByCategory(categoryName: String): Boolean = {
    buildParametersCategory.find(x => x.name == categoryName).exists(x => x.parts.nonEmpty)
  }

  def getTestsByCategory(categoryName: String): String = {
    buildParametersCategory.find(x => x.name == categoryName).map(x => if (x.parts.isEmpty) "" else "\"" + x.parts.mkString(" ") + "\"").getOrElse("All")
  }

  val getPossibleBuildParameters: List[BuildParametersCategory] = {
    List(
      BuildParametersCategory(Cycle.cycleTypeCategoryName, List("build full package")),
      BuildParametersCategory(Cycle.unitTestsCategoryName, getTests(Cycle.unitTestsCategoryName)),
      BuildParametersCategory(Cycle.funcTestsCategoryName,  getTests(Cycle.funcTestsCategoryName)),
      BuildParametersCategory(Cycle.cometCategoryName, List("Include")),
      BuildParametersCategory(Cycle.sliceCategoryName, List("Include")),
      BuildParametersCategory(Cycle.casperCategoryName, List("Include")),
      BuildParametersCategory(Cycle.dbCategoryName, List("Include"))
    )
  }

  def getTests(testName: String): List[String] = {
    val config = Play.configuration.getConfig("build").get
    config.getStringList(testName).get.asScala.toList.distinct
  }
}
