package buildboard2.components

import buildboard2.model.Account

trait AccountRepositoryComponent {
  val accountRepository: AccountRepository

  trait AccountRepository {
    def save(account: Account): Unit

    def findByToken(token: String): Option[Account]

    def remove(token: String): Unit

    def getAll: Iterator[Account]
  }

}