package models.cycles

import models.buildActions.BuildParametersCategory
import play.api.Play
import scala.collection.JavaConverters._
import play.api.Play.current


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

  def getPossibleBuildParameters: List[BuildParametersCategory] = {
    val config = Play.configuration.getConfig(s"build.cycle.$name").get
    List(
      BuildParametersCategory(Cycle.cycleTypeCategoryName, List("build full package")),
      BuildParametersCategory(Cycle.unitTestsCategoryName, config.getStringList(Cycle.unitTestsCategoryName).get.asScala.toList.distinct),
      BuildParametersCategory(Cycle.funcTestsCategoryName, config.getStringList(Cycle.funcTestsCategoryName).get.asScala.toList.distinct),
      BuildParametersCategory(Cycle.cometCategoryName, List("Include")),
      BuildParametersCategory(Cycle.sliceCategoryName, List("Include")),
      BuildParametersCategory(Cycle.casperCategoryName, List("Include")),
      BuildParametersCategory(Cycle.dbCategoryName, List("Include"))
    )
  }

}
