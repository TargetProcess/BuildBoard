package models.cycles

import components.CycleBuilderComponent
import models.buildActions.BuildParametersCategory
import play.api.{Configuration, Play}
import play.api.Play.current

import scala.collection.JavaConverters._
import scala.util.Try

trait ConfigurableCycleBuilderComponentImpl extends CycleBuilderComponent {

  def getTests(testName: String): List[String] = {
    Play.configuration.getConfig("build").get.getStringList(testName).get.asScala.toList.distinct
  }

  override val cycleBuilder = new CycleBuilder {
    def cycle(name: String): Cycle = {
      val config: Configuration = Play.configuration.getConfig(s"build.cycle.$name").get

      def getBoolean(path: String) = config.getBoolean(path).getOrElse(false)

      def getTests(path: String): String = {
        Try {
          config.getStringList(path).map(l => "\"" + l.asScala.mkString(" ") + "\"").get
        }.toOption
          .orElse(config.getString(path))
          .getOrElse("All")
      }
      val unitTests = getTests(CycleConstants.unitTestsCategoryName)
      val funcTests = getTests(CycleConstants.funcTestsCategoryName)
      val casperJsTests = getTests(CycleConstants.casperCategoryName)
      val karmaJsTests = getTests(CycleConstants.karmaCategoryName)
      val pythonFuncTests = getTests(CycleConstants.pythonFuncTestsCategoryName)
      val includeUnstable = getBoolean("includeUnstable")
      val buildFullPackage = getBoolean("buildFullPackage")
      val includeComet = getBoolean("includeComet")
      val includeSlice = getBoolean("includeSlice")
      val includeCasper = getBoolean("includeCasper")
      val includeDb = getBoolean("includeDb")
      val isFull = getBoolean("isFull")
      val includePerfTests = getBoolean("includePerfTests")

      Cycle(name, includeUnstable, buildFullPackage, unitTests, funcTests, casperJsTests, karmaJsTests, pythonFuncTests,
        includeComet, includeSlice, includeDb, isFull, includePerfTests)
    }

    def customCycle(buildParametersCategory: List[BuildParametersCategory]): Cycle = {


      def getBoolByCategory(categoryName: String): Boolean = {
        buildParametersCategory.find(x => x.name == categoryName).exists(x => x.parts.nonEmpty)
      }

      def getParamsByCategory(categoryName: String): Map[String, String] = {
        buildParametersCategory.filter(_.name == categoryName).flatMap(x => x.params).toMap
      }

      def getTestsByCategory(categoryName: String): String = {
        buildParametersCategory.find(x => x.name == categoryName).map(x => if (x.parts.isEmpty) "" else "\"" + x.parts.mkString(" ") + "\"").getOrElse("All")
      }

      val name = "Custom"

      val buildFullPackage = false
      val includeUnstable: Boolean = false

      val unitTests: String = getTestsByCategory(CycleConstants.unitTestsCategoryName)
      val includeComet: Boolean = getBoolByCategory(CycleConstants.cometCategoryName)
      val funcTests: String = getTestsByCategory(CycleConstants.funcTestsCategoryName)
      val pythonFuncTests: String = getTestsByCategory(CycleConstants.pythonFuncTestsCategoryName)
      val casperJsTests: String = getTestsByCategory(CycleConstants.casperCategoryName)
      val karmaJsTests: String = getTestsByCategory(CycleConstants.karmaCategoryName)
      val includeSlice: Boolean = getBoolByCategory(CycleConstants.sliceCategoryName)
      val includeDb: Boolean = getBoolByCategory(CycleConstants.dbCategoryName)
      val isFull: Boolean = getBoolByCategory(CycleConstants.cycleTypeCategoryName)
      val includePerfTests: Boolean = getBoolByCategory(CycleConstants.perfCategoryName)

      val possibleBuildParameters =
        List(
          BuildParametersCategory(CycleConstants.cycleTypeCategoryName, List("build full package")),
          BuildParametersCategory(CycleConstants.unitTestsCategoryName, getTests(CycleConstants.unitTestsCategoryName)),
          BuildParametersCategory(CycleConstants.funcTestsCategoryName, getTests(CycleConstants.funcTestsCategoryName)),
          BuildParametersCategory(CycleConstants.pythonFuncTestsCategoryName, getTests(CycleConstants.pythonFuncTestsCategoryName)),
          BuildParametersCategory(CycleConstants.cometCategoryName, List("Include")),
          BuildParametersCategory(CycleConstants.sliceCategoryName, List("Include")),
          BuildParametersCategory(CycleConstants.casperCategoryName, getTests(CycleConstants.casperCategoryName)),
          BuildParametersCategory(CycleConstants.karmaCategoryName, getTests(CycleConstants.karmaCategoryName)),
          BuildParametersCategory(CycleConstants.dbCategoryName, List("Include")),
          BuildParametersCategory(CycleConstants.perfCategoryName, List("Include"), Map("PerfTestClass" -> "", "PerfTestMethod" -> ""))
        )


      Cycle(name, includeUnstable, buildFullPackage, unitTests, funcTests, casperJsTests, karmaJsTests,
        pythonFuncTests, includeComet, includeSlice, includeDb, isFull, includePerfTests, buildParametersCategory, possibleBuildParameters)
    }
  }
}