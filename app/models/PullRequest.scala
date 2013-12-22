package models

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

case class PullRequest(name:String, prId: Int, url: String, created:DateTime) {}

case class PullRequestStatus(isMergeable:Boolean, isMerged:Boolean)

object PullRequest{
  def create(pr:PR):PullRequest = PullRequest(pr.getHead.getRef, pr.getNumber, pr.getHtmlUrl, new DateTime(pr.getCreatedAt))
}



object PullRequests extends ModelCompanion[PullRequest, ObjectId] {

  def collection = mongoCollection("pullRequests")


  val dao = new SalatDAO[PullRequest, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("prId" -> 1), "", unique = true)

  com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers()
  com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()

}
