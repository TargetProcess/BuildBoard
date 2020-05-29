package buildboard2.components

import play.api.Play.current
import com.mongodb.casbah.Imports._
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders.ObjectId
import buildboard2.model.Account
import models.mongo.mongoContext

trait AccountRepositoryComponentImpl extends AccountRepositoryComponent {
  val accountRepository = new AccountRepositoryImpl

  class AccountRepositoryImpl extends AccountRepository {
    import mongoContext.context

    object Accounts extends ModelCompanion[Account, ObjectId] {
      def collection = mongoCollection("accounts2")

      val dao = new SalatDAO[Account, ObjectId](collection) {}
    }

    override def save(account: Account) = Accounts.save(account)

    override def findByToken(token: String): Option[Account] = Accounts.findOne(MongoDBObject("toolToken" -> token))

    override def remove(token: String): Unit = Accounts.remove(MongoDBObject("toolToken" -> token))

    override def getAll: Iterator[Account] = Accounts.findAll()
  }

}
