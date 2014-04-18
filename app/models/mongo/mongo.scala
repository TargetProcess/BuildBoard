package models.mongo

import com.novus.salat.{TypeHintFrequency, Context}
import play.api.Play
import play.api.Play.current
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import models._
import se.radley.plugin.salat._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat.Binders.ObjectId
import models.Branch
import com.novus.salat.StringTypeHintStrategy
import models.Build
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers

object mongoContext {
  implicit val context = {
    val context = new Context {
      val name = "global"
      override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    }
    context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
    context.registerClassLoader(Play.classloader)
    RegisterJodaTimeConversionHelpers()

    context
  }
}

import mongoContext._

trait Collection[T] {
  def save(value: T): Any

  def remove(value: T): Any

  def findAll: Iterator[T]
}

object Builds extends ModelCompanion[Build, ObjectId] with Collection[Build] {
  def collection = mongoCollection("builds")

  val dao = new SalatDAO[Build, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("number" -> 1, "branch" -> 1), "build_number", unique = true)
}

object Branches extends ModelCompanion[Branch, ObjectId] with Collection[Branch] {
  def collection = mongoCollection("branches")

  val dao = new SalatDAO[Branch, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("name" -> 1), "", unique = true)
}

trait UserDAO extends ModelCompanion[User, ObjectId] {
  def collection = mongoCollection("users")

  val dao = new SalatDAO[User, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("tpId" -> 1), "user_id", unique = true)

  // Queries
  def findOneByUsername(username: String): Option[User] = dao.findOne(MongoDBObject("username" -> username))

  def findOneById(id: Int): Option[User] = dao.findOne(MongoDBObject("tpId" -> id))
}

object Users extends UserDAO {
  def saveLogged(tpUser: TpUser, token: String) = {

    val userFromDb = Users.findOneById(tpUser.id)
    val newUser =
      userFromDb match {
        case None =>
          User(tpId = tpUser.id, username = tpUser.login, token = token, fullName = tpUser.fullName)
        case Some(user) =>
          user.copy(username = tpUser.login, token = token, fullName = tpUser.fullName)
      }
    Users.save(newUser)
  }
}