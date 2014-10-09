package models.buildWatcher

import components.{BuildRepositoryComponent, BuildWatcherComponent, JenkinsServiceComponent}
import models.buildActions.{BuildParametersCategory, ReuseArtifactsBuildAction}
import models.cycles.{CustomCycle, Cycle}
import models.{BuildStatus, BranchInfo, Build}
import play.api.Play.current
import play.api.{Logger, Play}

import scala.collection.JavaConverters._

trait BuildWatcherComponentImpl extends BuildWatcherComponent {
  this: BuildWatcherComponentImpl
    with BuildRepositoryComponent
    with JenkinsServiceComponent
  =>

  override val buildWatcher: BuildWatcher = new BuildWatcherImpl


  class BuildWatcherImpl extends BuildWatcher {

    def shouldRerunBuild(build: Build): Boolean = {
      build.branch match {
        case BranchInfo.develop() => true
        case BranchInfo.hotfix(_) => true
        case BranchInfo.release(_) => true
        case BranchInfo.feature(_) => true
        case _ => true
      }


    }

    def getFailedTests(build: Build, category: String): List[String] = {

      val testParts = Play.configuration.getConfig("build")
        .flatMap(_.getStringList(category))
        .map(_.asScala.toList)
        .getOrElse(Nil)

      val testRootNode = build.node.flatMap(_.allChildren.find(_.name.compareToIgnoreCase("funcTests") == 0))
      val failedNodes = testRootNode.map(_.allChildren.filter(
        bn => !bn.buildStatus.success.getOrElse(true) && testParts.contains(bn.name) && (bn.rerun.isDefined && !bn.rerun.get)
      ))

      failedNodes.map(_.map(_.name).toList).getOrElse(Nil)
    }

    override def rerunFailedParts(updatedBuild: Build) {
      if (shouldRerunBuild(updatedBuild)) {

        if (updatedBuild.buildStatus != BuildStatus.InProgress) {
          val funcTestsToRerun = getFailedTests(updatedBuild, "funcTests")
          val unitTestsToRerun = getFailedTests(updatedBuild, "unitTests")

          val action = ReuseArtifactsBuildAction(updatedBuild.name, updatedBuild.number, CustomCycle(List(
            BuildParametersCategory(Cycle.funcTestsCategoryName, funcTestsToRerun),
            BuildParametersCategory(Cycle.unitTestsCategoryName, unitTestsToRerun)
          )))

          if (funcTestsToRerun.nonEmpty || unitTestsToRerun.nonEmpty) {
            Logger.info(s"Rerun: $action")
            //jenkinsService.forceBuild(action)
          }
        }
      }
    }
  }

}
