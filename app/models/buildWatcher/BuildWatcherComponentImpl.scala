package models.buildWatcher

import components.{BuildWatcherComponent, JenkinsServiceComponent, RerunRepositoryComponent}
import models.buildActions.{BuildParametersCategory, ReuseArtifactsBuildAction}
import models.cycles.{CustomCycle, Cycle}
import models.{BranchInfo, Build, BuildStatus}
import play.api.Play.current
import play.api.{Logger, Play}

import scala.collection.JavaConverters._

trait BuildWatcherComponentImpl extends BuildWatcherComponent {
  this: BuildWatcherComponentImpl
    with JenkinsServiceComponent
    with RerunRepositoryComponent
  =>


  override val buildWatcher: BuildWatcher = new BuildWatcher {

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

    override def rerunFailedParts(updatedBuild: Build) {
      if (shouldRerunBuild(updatedBuild)) {


        updatedBuild.selfBuildStatus match {

          case BuildStatus.Failure | BuildStatus.TimedOut =>
            val funcTestsToRerun = getNodesToRerun(updatedBuild, Cycle.funcTestsCategoryName)
            val unitTestsToRerun = getNodesToRerun(updatedBuild, Cycle.unitTestsCategoryName)

            rerunRepository.markAsRerun(updatedBuild, Cycle.funcTestsCategoryName, funcTestsToRerun)
            rerunRepository.markAsRerun(updatedBuild, Cycle.unitTestsCategoryName, unitTestsToRerun)


            val action = ReuseArtifactsBuildAction(updatedBuild.name, updatedBuild.number, CustomCycle(List(
              BuildParametersCategory(Cycle.funcTestsCategoryName, funcTestsToRerun),
              BuildParametersCategory(Cycle.unitTestsCategoryName, unitTestsToRerun)
            )))

            if (funcTestsToRerun.nonEmpty || unitTestsToRerun.nonEmpty) {
              Logger.info(s"Rerun: $action")
              jenkinsService.forceBuild(action)
            }
          case _ =>
        }
      }
    }
  }

}
