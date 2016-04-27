package repositories

import globals.context
import models.{Branch, BranchRepositoryComponentImpl, Build, BuildRepositoryComponentImpl}
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.specs2.mutable._
import play.api.test.Helpers._

class BranchesRepositoryImplSpec extends Specification with Mockito {
  sequential

  "BranchesRepositoryImpl" should {
    "return last build" in context {
      running(context.fakeApp) {

        val repo = new BranchRepositoryComponentImpl with BuildRepositoryComponentImpl

        repo.buildRepository.getBuilds.size must_== 0

        repo.buildRepository update Build(number = 1, branch = "Branch1", status = Some("Success"), timestamp = DateTime.now, timestampEnd = None, name = "build #1", node = None)
        repo.buildRepository update Build(number = 2, branch = "Branch1", status = Some("Failure"), timestamp = DateTime.now, timestampEnd = None, name = "build #2", node = None)

        repo.buildRepository update Build(number = 3, branch = "Branch2", status = Some("Success"), timestamp = DateTime.now, timestampEnd = None, name = "build #3", node = None)
        repo.buildRepository update Build(number = 4, branch = "Branch2", status = Some("Failure"), timestamp = DateTime.now, timestampEnd = None, name = "build #4", node = None)


        repo.buildRepository.getBuilds.size must_== 4


        repo.branchRepository.update(Branch(name = "Branch1", url = ""))
        repo.branchRepository.update(Branch(name = "Branch2", url = ""))

        val branches = repo.branchRepository.getBranchesWithLastBuild

        branches.size must_== 2

        branches.find(_.name == "Branch1").flatMap(_.lastBuild.map(_.name)) must_== Some("build #2")
        branches.find(_.name == "Branch2").flatMap(_.lastBuild.map(_.name)) must_== Some("build #4")

      }
    }
  }
}



