package buildWatcher

import components.{JenkinsServiceComponent, RerunRepositoryComponent}
import globals.context
import models.buildActions.BuildAction
import models.buildWatcher.BuildWatcherComponentImpl
import models.{Build, BuildNode}
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.test.Helpers._

class BuildWatcherImplSpec extends Specification
with Mockito {

  def node(name: String, status: Option[String], children: BuildNode*) = BuildNode(name, "", status, "", Nil, null, None, children = children.toList)


  "BuildWatcher" should {
    "not run on 'in progress' builds" in context {
      running(context.fakeApp) {

        val buildWatcherComponent = getBuildWatcher


        val fail: Some[String] = Some("Failure")
        val inProgress = None

        val buildToRerun = Build(1, "branch", inProgress, DateTime.now(), name = "build 1", node = Some(
          node("StartBuild", inProgress,
            node("FuncTests", fail,
              node("Part1", fail)
            )
          )
        ))

        buildWatcherComponent.buildWatcher.rerunFailedParts(buildToRerun)

        there was no(buildWatcherComponent.jenkinsService).forceBuild(any[BuildAction])

      }
    }
  }

  def getBuildWatcher: BuildWatcherComponentImpl with JenkinsServiceComponent with RerunRepositoryComponent = {
    val buildWatcherComponent = new BuildWatcherComponentImpl with JenkinsServiceComponent with RerunRepositoryComponent {
      override val jenkinsService: JenkinsService = mock[JenkinsService]
      override val rerunRepository: RerunRepository = mock[RerunRepository]
    }
    buildWatcherComponent
  }
}
