package models.buildActions

import models.cycles.{Cycle, CustomCycle}
import play.api.Play
import scala.collection.JavaConverters._
import play.api.Play.current

trait CustomBuildAction extends BuildAction {
  val cycle: CustomCycle

  def getPossibleBuildParameters: List[BuildParametersCategory] = {
    val cycleName = cycle.name
    val config = Play.configuration.getConfig(s"build.cycle.$cycleName").get
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
