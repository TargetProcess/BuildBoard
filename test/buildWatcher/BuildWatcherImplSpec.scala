package buildWatcher

import components.{CycleBuilderComponent, JenkinsServiceComponent, RerunRepositoryComponent}
import globals.context
import models.buildActions.{ReuseArtifactsBuildAction, JenkinsBuildAction}
import models.buildRerun.{BuildRerunComponentImpl, RerunRepositoryComponentImpl}
import models.cycles.ConfigurableCycleBuilderComponentImpl
import models.{Build, BuildNode}
import org.joda.time.DateTime
import org.specs2.matcher.{Expectable, MatchResult, Matcher}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.test.Helpers._

class BuildWatcherImplSpec extends Specification
with Mockito {

  def node(name: String, status: Option[String], children: BuildNode*) = BuildNode("", name, "", 12, status, "", Nil, null, None, children = children.toList)

  def build(status: Option[String], node: BuildNode) = Build(1, "branch", status, DateTime.now(), name = "build 1", node = Some(node))

  val fail = Some("Failure")
  val succ = Some("Success")
  val inProgress = None


  "BuildWatcher" should {
    "should rerun failed builds" in context {
      running(context.fakeApp) {
        val buildWatcherComponent = getBuildWatcher

        val buildToRerun = build(fail,
          node("StartBuild", inProgress,
            node("FuncTests", fail,
              node("Part1", fail),
              node("Part2", succ),
              node("SomeDiffPart", fail)
            ),
            node("UnitTests", fail,
              node("Part2", fail),
              node("Part3", succ),
              node("AnotherPart", fail))
          )
        )

        buildWatcherComponent.buildRerun.rerunFailedParts(buildToRerun)

        there was one(buildWatcherComponent.jenkinsService).forceBuild(argThat(isRerunFor(List("Part1"), List("Part2"))))

      }
    }


    "should not rerun twice" in context {
      running(context.fakeApp) {
        val watcher = new BuildRerunComponentImpl with JenkinsServiceComponent with RerunRepositoryComponentImpl with ConfigurableCycleBuilderComponentImpl {
          override val jenkinsService: JenkinsService = mock[JenkinsService]

        }

        val buildToRerun = build(fail,
          node("StartBuild", inProgress,
            node("FuncTests", fail,
              node("Part1", fail),
              node("Part2", succ),
              node("SomeDiffPart", fail)
            ),
            node("UnitTests", fail,
              node("Part2", fail),
              node("Part3", succ),
              node("AnotherPart", fail))
          )
        )

        watcher.buildRerun.rerunFailedParts(buildToRerun)
        watcher.buildRerun.rerunFailedParts(buildToRerun)

        there was one(watcher.jenkinsService).forceBuild(argThat(isRerunFor(List("Part1"), List("Part2"))))

      }
    }
  }

  "should not rerun 'rerun' parts" in context {
    running(context.fakeApp) {
      val buildWatcherComponent = getBuildWatcher

      val buildToRerun = build(fail,
        node("StartBuild", inProgress,
          node("FuncTests", fail,
            node("Part1", fail).copy(rerun = Some(true)),
            node("Part2", succ),
            node("SomeDiffPart", fail)
          ),
          node("UnitTests", fail,
            node("Part2", fail),
            node("Part3", succ),
            node("AnotherPart", fail))
        )
      )
      buildWatcherComponent.buildRerun.rerunFailedParts(buildToRerun)

      there was one(buildWatcherComponent.jenkinsService).forceBuild(argThat(isRerunFor(Nil, List("Part2"))))

    }
  }

  def isRerunFor(funcTests: List[String] = Nil, unitTests: List[String] = Nil) = new Matcher[JenkinsBuildAction] {
    override def apply[S <: JenkinsBuildAction](t: Expectable[S]): MatchResult[S] = {
      val expected = t.value.asInstanceOf[ReuseArtifactsBuildAction]


      val funcTestsNotFound = funcTests.filterNot(expected.cycle.funcTests.contains(_))
      val unitTestsNotFound = unitTests.filterNot(expected.cycle.unitTests.contains(_))


      val funcTestsMessage = if (funcTestsNotFound.isEmpty) None else Some(s"FuncTests not found: ${funcTestsNotFound.mkString(", ")}")
      val unitTestsMessage = if (unitTestsNotFound.isEmpty) None else Some(s"UnitTests not found: ${unitTestsNotFound.mkString(", ")}")

      val resultString = List(funcTestsMessage, unitTestsMessage).flatten.mkString(" and ")

      result(resultString.isEmpty, s"Rerun is correct", resultString, t)


    }
  }


  def getBuildWatcher: BuildRerunComponentImpl with JenkinsServiceComponent with RerunRepositoryComponent = {
    new BuildRerunComponentImpl with JenkinsServiceComponent with RerunRepositoryComponent with ConfigurableCycleBuilderComponentImpl {
      override val jenkinsService: JenkinsService = mock[JenkinsService]
      override val rerunRepository: RerunRepository = mock[RerunRepository]
    }
  }
}
