package services

import org.specs2.mutable.Specification
import models._
import org.specs2.mock.Mockito
import models.magicMerge.MagicMergeComponentImpl
import components.{TargetprocessComponent, GithubServiceComponent, BranchRepositoryComponent}
import org.joda.time.DateTime
import models.PullRequestStatus
import models.PullRequest
import models.User
import scala.util.Success
import models.Branch
import scala.util.Failure
import scala.Some

class MagicMergeServiceSpec extends Specification with Mockito {

  "MagicMergeService" should {
    "should merge close and delete" in {

      val user: User = User(tpId = 1, username = "tp", token = "token")

      val magicMerge = new MagicMergeComponentImpl with BranchRepositoryComponent with GithubServiceComponent with TargetprocessComponent {
        override val branchRepository: BranchRepository = mock[BranchRepository]

        branchRepository.getBranch("branch")
          .returns(
            Some(
              Branch("branch", "http://branch",
                pullRequest = Some(PullRequest("branch", 1, "http://pr1",
                  timestamp = DateTime.now(),
                  status = PullRequestStatus(isMergeable = true, isMerged = false)
                )),
                entity = Some(Entity(10, "entity", "UserStory",
                  url = "http://tp",
                  state = EntityState(1, "Tested", isFinal = false,
                    nextStates = List(EntityState(2, "Final", isFinal = true)))
                ))

              )
            )
          )

        override val githubService: GithubService = mock[GithubService]

        githubService.mergePullRequest(1, user).returns(MergeResult(isMerged = true, "Merged", "sha"))


        val entityRepo = mock[EntityRepository]

        override def entityRepository: EntityRepository = entityRepo
      }


      magicMerge.magicMergeService.merge("branch", user) match {
        case Success(mergeResult) =>
          mergeResult.merged mustEqual true
          mergeResult.closed mustEqual true
          mergeResult.deleted mustEqual true

          there was one(magicMerge.githubService).deleteBranch("branch")
          there was one(magicMerge.entityRepo).changeEntityState(10, 2)

        case Failure(e) => ko(e.toString)
      }


    }
  }


}