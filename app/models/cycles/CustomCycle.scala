package models.cycles

import models.buildActions.BuildParametersCategory

case class CustomCycle(buildParametersCategory: List[BuildParametersCategory]) extends Cycle {

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
}
