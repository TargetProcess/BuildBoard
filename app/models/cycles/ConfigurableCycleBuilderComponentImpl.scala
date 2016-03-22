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
        CycleParameters(isFull, includeUnstable, includeDb, includeComet, includeSlice, includePerfTests, buildFullPackage, casperJsTests,karmaJsTests, unitTests, pythonFuncTests, funcTests),
        buildParametersCategory, possibleBuildParameters)
    }

    def buildCycle(cf: CycleConfig): Cycle = Cycle(cf.name, cf.parameters, Nil, Nil)

    override def buildActions(branch: Branch): List[BuildAction] = {

      val branchCategories = config.buildConfig.branches
        .filter { case (name, regex) =>
          regex.r.findFirstIn(branch.name).isDefined
        }.keys
        .toList


      val applicableBranchConfigs = config.buildConfig.build.cycles
        .filter(cycleConfig => {
          cycleConfig.branches.contains("all") || cycleConfig.branches.intersect(branchCategories).nonEmpty
        })

      val branchCycles: List[Cycle] = applicableBranchConfigs.map(cf => buildCycle(cf))

      val customCycles = config.buildConfig.build.customCyclesAvailability

      val branchBuildActions: List[BranchBuildAction] = branchCycles.map(BranchBuildAction(branch.name, _))



      val pullRequestBuildAction = branch.pullRequest
        .filter(_.status.isMergeable).toList.flatMap(pr => branchCycles.map(PullRequestBuildAction(pr.prId, _)))


      branchBuildActions ++
        pullRequestBuildAction ++
        List(TransifexBuildAction(branch.name))


      /*









            val packageOnlyCycle: Cycle = cycleBuilderComponent.cycleBuilder.packageOnlyCycle
            val fullCycle: Cycle = cycleBuilderComponent.cycleBuilder.fullCycle
            val shortCycle: Cycle = cycleBuilderComponent.cycleBuilder.shortCycle

            val buildPackages = List(
              BranchBuildAction(name, packageOnlyCycle),
              BranchBuildAction(name, fullCycle)
            )

            val buildBranches = name match {
              case BranchInfo.release(_) => Nil
              case BranchInfo.hotfix(_) => Nil
              case _ => List(BranchBuildAction(name, shortCycle))
            }

            val (buildPullRequests, buildPullRequestCustom) =
              pullRequest match {
                case Some(pr) if pr.status.isMergeable => (
                  List(
                    PullRequestBuildAction(pr.prId, shortCycle),
                    PullRequestBuildAction(pr.prId, fullCycle)
                  ),
                  List(PullRequestBuildAction(pr.prId, cycleBuilderComponent.cycleBuilder.emptyCustomCycle))
                  )
                case _ => (Nil, Nil)
              }

            val buildCustomBranch = name match {
              case BranchInfo.release(_) => Nil
              case BranchInfo.hotfix(_) => Nil
              case BranchInfo.develop() => Nil
              case _ => List(
                BranchBuildAction(name, cycleBuilderComponent.cycleBuilder.emptyCustomCycle)
              )
            }

            buildPackages ++
              buildBranches ++
              buildPullRequests ++
              buildCustomBranch ++
              buildPullRequestCustom ++
              List(TransifexBuildAction(name))
              */
    }
    override def find(cycleName: String): Option[Cycle]  = config.buildConfig.build.cycles.find(_.name == cycleName).map(buildCycle(_))
  }
}