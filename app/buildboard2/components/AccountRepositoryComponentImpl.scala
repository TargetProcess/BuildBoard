package buildboard2.components

import buildboard2.Account
import models.mongo.mongoContext
import play.api.Play.current
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import se.radley.plugin.salat._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat.Binders.ObjectId

trait AccountRepositoryComponentImpl extends AccountRepositoryComponent {
  val accountRepository = new AccountRepositoryImpl

  class AccountRepositoryImpl extends AccountRepository {
    import mongoContext.context

    object Accounts extends ModelCompanion[Account, ObjectId] {
      def collection = mongoCollection("accounts")

      val dao = new SalatDAO[Account, ObjectId](collection) {}
    }

    override def save(account: Account) = Accounts.save(account)

    override def findByToken(token: String): Option[Account] = Accounts.findOne(MongoDBObject("toolToken" -> token))

    override def remove(token: String): Unit = Accounts.remove(MongoDBObject("toolToken" -> token))
  }

}
