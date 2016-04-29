package models.buildRerun

import components._
import models.buildActions.{BuildParametersCategory, ReuseArtifactsBuildAction}
import models.cycles.CycleConstants
import models.{BranchInfo, Build}
import play.api.Logger

trait AutoBuildRerunComponentImpl extends BuildRerunComponent {
  this: AutoBuildRerunComponentImpl
    with ForceBuildComponent
    with RerunRepositoryComponent
    with CycleBuilderComponent
    with ConfigComponent
  =>


  override val buildRerun: BuildRerun = new BuildRerun {

    override def rerunFailedParts(updatedBuild: Build) = {

      if (shouldRerunBuild(updatedBuild)) {

        val testsToRerun = CycleConstants.allTestCategories.values.map(category => (getNodesToRerun(updatedBuild, category.name), category))

        if (testsToRerun.exists(_._1.nonEmpty)) {

          testsToRerun.foreach { case (tests, category) => rerunRepository.markAsRerun(updatedBuild, category.name, tests) }

          val buildParametersCategory = testsToRerun.map { case (tests, category) => BuildParametersCategory(category.name, None, tests) }

          val action = ReuseArtifactsBuildAction(updatedBuild.name, updatedBuild.number, cycleBuilder.customCycle(buildParametersCategory.toList))

          Logger.info(s"Rerun: $action")
          forceBuildService.forceBuild(action)
        }
      }
    }


    def autoRerun(name: String): Boolean = config.buildConfig.autoRerun(name)

    def shouldRerunBuild(build: Build): Boolean = {
      val branch = build.branch match {
        case BranchInfo.develop() => "develop"
        case BranchInfo.hotfix(_) => "hotfix"
        case BranchInfo.release(_) => "release"
        case BranchInfo.feature(_) => "feature"
        case BranchInfo.vs(_) => "vs"
        case _ => "others"
      }
      autoRerun(branch)
    }

    def getNodesToRerun(build: Build, category: String): List[String] = {

      val testParts = config.buildConfig.build.tests(category)

      val testRootNode = build.node.flatMap(_.allChildren.find(_.name.compareToIgnoreCase(category) == 0))
      val failedNodes = testRootNode.map(_.allChildren.filter(
        bn =>
          !bn.buildStatus.success.getOrElse(true)
            && testParts.contains(bn.name)
            && !rerunRepository.contains(build, category, bn.name)
      )
      )

      failedNodes.map(_.map(_.name).toList).getOrElse(Nil)
    }

  }

}
