package models.github

import models._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import se.radley.plugin.salat.Binders._
import models.PullRequestStatus
import se.radley.plugin.salat._
import models.PullRequestStatus
import com.mongodb.casbah.Imports._
import models.PullRequestStatus
import se.radley.plugin.salat.Binders.ObjectId
import org.eclipse.egit.github.core.{PullRequest=>PR}
import com.github.nscala_time.time.Imports._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import se.radley.plugin.salat.Binders._
import se.radley.plugin.salat._
import com.mongodb.casbah.Imports._
import play.api.Play.current
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import mongoContext._

case class GithubBranch(name:String)

object GithubBranch {
  def create(branch:org.eclipse.egit.github.core.RepositoryBranch) = GithubBranch(branch.getName)
}

trait GithubRepository {
  def getBranches: List[GithubBranch]
  def getPullRequests: List[PullRequest]
  def getPullRequestStatus(id: Int):PullRequestStatus
  def getUrlForBranch(name:String) =  GithubApplication.url(name)
}

object GithubBranches extends ModelCompanion[GithubBranch, ObjectId] {

  def collection = mongoCollection("branches")


  val dao = new SalatDAO[GithubBranch, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("name" -> 1), "", unique = true)
}

