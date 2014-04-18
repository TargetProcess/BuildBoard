package models.tp

import scalaj.http.{HttpOptions, Http}
import play.api.libs.json._
import scala.util._
import TargetprocessApplication._
import models.AuthInfo
import play.api.Play.current
import components.{AuthInfoProviderComponent, UserRepositoryComponent}
import models.mongo.mongoContext
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import se.radley.plugin.salat.Binders._
import se.radley.plugin.salat._
import com.mongodb.casbah.Imports._
import models.User
import se.radley.plugin.salat.Binders.ObjectId
import scala.Some
import models.TpUser

trait UserRepositoryComponentImpl extends UserRepositoryComponent {

  val userRepository: UserRepository = new UserRepositoryImpl


  class UserRepositoryImpl extends UserRepository {

    import mongoContext._

    object Users extends ModelCompanion[User, ObjectId] {
      def collection = mongoCollection("users")

      val dao = new SalatDAO[User, ObjectId](collection) {}

      // Indexes
      collection.ensureIndex(DBObject("tpId" -> 1), "user_id", unique = true)
    }


    def authenticate(username: String, password: String): Try[(TpUser, String)] = Try {
      val response = Http(apiUri("Authentication") + "?format=json")
        .auth(username, password)
        .option(HttpOptions.connTimeout(1000))
        .option(HttpOptions.readTimeout(5000))
        .asString

      val json = Json.parse(response)
      val tpToken = (json \ "Token").as[String]
      val user = new TargetprocessComponentImpl with AuthInfoProviderComponent {
        val authInfo: AuthInfo = new AuthInfo {
          val token = tpToken
          val githubToken = ""
        }
      }
        .entityRepository.getLoggedUser

      user
    }

    override def save(tpUser: TpUser, token: String): Unit = {

      val userFromDb = findOneById(tpUser.id)
      val newUser =
        userFromDb match {
          case None =>
            User(tpId = tpUser.id, username = tpUser.login, token = token, fullName = tpUser.fullName)
          case Some(user) =>
            user.copy(username = tpUser.login, token = token, fullName = tpUser.fullName)
        }
      save(newUser)
    }

    def findOneByUsername(username: String): Option[User] = Users.findOne(MongoDBObject("username" -> username))

    def findOneById(id: Int): Option[User] = Users.findOne(MongoDBObject("tpId" -> id))

    def save(user: User): Unit = Users.save(user)

  }

}

