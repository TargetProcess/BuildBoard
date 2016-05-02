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

      val tests = CycleConstants.allTestCategories.map{ case(categoryName,category) => (category, getTestsByCategory(category.name))}

      val includeComet: Boolean = getBoolByCategory(CycleConstants.cometCategoryName)
      val includeSlice = getBoolByCategory(CycleConstants.sliceCategoryName)
      val includeDb = getBoolByCategory(CycleConstants.dbCategoryName)
      val isFull: Boolean = getBoolByCategory(CycleConstants.cycleTypeCategoryName)
      val includePerfTests: Boolean = getBoolByCategory(CycleConstants.perfCategoryName)
      val includeMashupTests: Boolean = getBoolByCategory(CycleConstants.includeMashupTestsKey)

      val possibleBuildParameters =
        List(
          BuildParametersCategory(CycleConstants.cycleTypeCategoryName, None, List("build full package"))
        ) ++
          CycleConstants.allTestCategories.values.map(category => BuildParametersCategory(category.name, Some(category.runName), getTests(category.name))).toList ++
          List(
            BuildParametersCategory(CycleConstants.cometCategoryName, None, List("Include")),
            BuildParametersCategory(CycleConstants.sliceCategoryName, None, List("Include")),
            BuildParametersCategory(CycleConstants.dbCategoryName, None, List("Include")),
            BuildParametersCategory(CycleConstants.perfCategoryName, None, List("Include"), Map("PerfTestClass" -> "", "PerfTestMethod" -> ""))
          )


      Cycle(name,
        CycleParameters(isFull, includeUnstable, includeDb, includeComet, includeSlice, includePerfTests, includeMashupTests, buildFullPackage, tests),
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
