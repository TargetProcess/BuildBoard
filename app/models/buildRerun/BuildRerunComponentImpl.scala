package models.buildRerun

import components.{CycleBuilderComponent, BuildRerunComponent, JenkinsServiceComponent, RerunRepositoryComponent}
import models.buildActions.{BuildParametersCategory, ReuseArtifactsBuildAction}
import models.cycles.CycleConstants
import models.{BranchInfo, Build}
import play.api.Play.current
import play.api.{Logger, Play}

import scala.collection.JavaConverters._

trait BuildRerunComponentImpl extends BuildRerunComponent {
  this: BuildRerunComponentImpl
    with JenkinsServiceComponent
    with RerunRepositoryComponent
    with CycleBuilderComponent
  =>


  override val buildRerun: BuildRerun = new BuildRerun {

    override def rerunFailedParts(updatedBuild: Build) {
      if (shouldRerunBuild(updatedBuild)) {

        val funcTestsToRerun = getNodesToRerun(updatedBuild, CycleConstants.funcTestsCategoryName)
        val pythonFuncTestsToRerun = getNodesToRerun(updatedBuild, CycleConstants.pythonFuncTestsCategoryName)
        val unitTestsToRerun = getNodesToRerun(updatedBuild, CycleConstants.unitTestsCategoryName)
        val casperTestsToRerun = getNodesToRerun(updatedBuild, CycleConstants.casperCategoryName)
        val karmaTestsToRerun = getNodesToRerun(updatedBuild, CycleConstants.karmaCategoryName)


        if (funcTestsToRerun.nonEmpty
          || pythonFuncTestsToRerun.nonEmpty
          || unitTestsToRerun.nonEmpty
          || casperTestsToRerun.nonEmpty
          || karmaTestsToRerun.nonEmpty
        ) {

          rerunRepository.markAsRerun(updatedBuild, CycleConstants.funcTestsCategoryName, funcTestsToRerun)
          rerunRepository.markAsRerun(updatedBuild, CycleConstants.pythonFuncTestsCategoryName, pythonFuncTestsToRerun)
          rerunRepository.markAsRerun(updatedBuild, CycleConstants.unitTestsCategoryName, unitTestsToRerun)

          val action = ReuseArtifactsBuildAction(updatedBuild.name, updatedBuild.number, cycleBuilder.customCycle(List(
            BuildParametersCategory(CycleConstants.funcTestsCategoryName, funcTestsToRerun),
            BuildParametersCategory(CycleConstants.pythonFuncTestsCategoryName, pythonFuncTestsToRerun),
            BuildParametersCategory(CycleConstants.unitTestsCategoryName, unitTestsToRerun),
            BuildParametersCategory(CycleConstants.casperCategoryName, casperTestsToRerun),
            BuildParametersCategory(CycleConstants.karmaCategoryName, karmaTestsToRerun)
          )))

          Logger.info(s"Rerun: $action")
          jenkinsService.forceBuild(action)
        }
      }
    }


    val autoRerunConfig = Play.configuration.getConfig("autoRerun")

    def autoRerun(name: String): Boolean = autoRerunConfig.flatMap(_.getBoolean(name)).getOrElse(false)


    def shouldRerunBuild(build: Build): Boolean = {


      build.branch match {
        case BranchInfo.develop() => autoRerun("develop")
        case BranchInfo.hotfix(_) => autoRerun("hotfix")
        case BranchInfo.release(_) => autoRerun("release")
        case BranchInfo.feature(_) => autoRerun("feature")
        case BranchInfo.vs(_) => autoRerun("vs")
        case _ => autoRerun("others")
      }


    }

    def getNodesToRerun(build: Build, category: String): List[String] = {

      val testParts = Play.configuration.getConfig("build")
        .flatMap(_.getStringList(category))
        .map(_.asScala.toList)
        .getOrElse(Nil)

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
