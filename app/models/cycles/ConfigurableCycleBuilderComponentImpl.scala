package models.cycles

import components.{ConfigComponent, CycleBuilderComponent}
import models.Branch
import models.buildActions._
import models.configuration.{CycleConfig, CycleParameters}

trait ConfigurableCycleBuilderComponentImpl extends CycleBuilderComponent {

  this: ConfigurableCycleBuilderComponentImpl with ConfigComponent =>

  def getTests(testName: String): List[String] = config.buildConfig.build.tests(testName)

  override val cycleBuilder = new CycleBuilder {
    def customCycle(buildParametersCategory: List[BuildParametersCategory]): Cycle = {

      def getBoolByCategory(categoryName: String): Boolean = {
        buildParametersCategory.find(x => x.name == categoryName).exists(x => x.parts.nonEmpty)
      }

      def getParamsByCategory(categoryName: String): Map[String, String] = {
        buildParametersCategory.filter(_.name == categoryName).flatMap(x => x.params).toMap
      }

      def getTestsByCategory(categoryName: String): List[String] = {
        buildParametersCategory.find(x => x.name == categoryName).map(x => if (x.parts.isEmpty) Nil else x.parts).getOrElse(List("All"))
      }

      val name = "Custom"

      val buildFullPackage = false
      val includeUnstable: Boolean = false

      val unitTests = getTestsByCategory(CycleConstants.unitTestsCategoryName)
      val includeComet: Boolean = getBoolByCategory(CycleConstants.cometCategoryName)
      val funcTests = getTestsByCategory(CycleConstants.funcTestsCategoryName)
      val pythonFuncTests = getTestsByCategory(CycleConstants.pythonFuncTestsCategoryName)
      val casperJsTests = getTestsByCategory(CycleConstants.casperCategoryName)
      val karmaJsTests = getTestsByCategory(CycleConstants.karmaCategoryName)
      val includeSlice = getBoolByCategory(CycleConstants.sliceCategoryName)
      val includeDb = getBoolByCategory(CycleConstants.dbCategoryName)
      val isFull: Boolean = getBoolByCategory(CycleConstants.cycleTypeCategoryName)
      val includePerfTests: Boolean = getBoolByCategory(CycleConstants.perfCategoryName)
      val includeMashupTests: Boolean = getBoolByCategory(CycleConstants.includeMashupTestsKey)

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


      Cycle(name,
        CycleParameters(isFull, includeUnstable, includeDb, includeComet, includeSlice, includePerfTests, includeMashupTests, buildFullPackage, casperJsTests, karmaJsTests, unitTests, pythonFuncTests, funcTests),
        buildParametersCategory, possibleBuildParameters)
    }

    def buildCycle(cf: CycleConfig): Cycle = Cycle(cf.name, cf.parameters, Nil, Nil)

    override def buildActions(branch: Branch): List[BuildAction] = {


      val applicableBranchConfigs = config.buildConfig.build.cycles
        .filter(cycleConfig => config.buildConfig.isApplicable(branch.name, cycleConfig.branches))

      val branchCycles: List[Cycle] = applicableBranchConfigs.map(cf => buildCycle(cf)) ++
        (if (config.buildConfig.isApplicable(branch.name, config.buildConfig.build.customCyclesAvailability))
          List(customCycle(Nil))
        else
          Nil)


      val branchBuildActions = branchCycles.map(BranchBuildAction(branch.name, _))
      val pullRequestBuildAction = branch.pullRequest
        .filter(_.status.isMergeable)
        .toList
        .flatMap(pr => branchCycles.map(PullRequestBuildAction(pr.prId, _)))


      branchBuildActions ++
        pullRequestBuildAction
    }

    override def find(cycleName: String): Option[Cycle] = config.buildConfig.build.cycles.find(_.name == cycleName).map(buildCycle(_))
  }
}
