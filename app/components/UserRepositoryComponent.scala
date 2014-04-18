package components

import models.{User, TpUser}
import scala.util.Try

trait UserRepositoryComponent {
  val userRepository: UserRepository

  trait UserRepository {
    def authenticate(username: String, password: String): Try[(TpUser, String)]

    def save(tpUser: TpUser, token: String)

    def save(tpUser: User)

    def findOneByUsername(username: String): Option[User]

    def findOneById(id: Int): Option[User]

    val loggedUser: Option[User]
  }

}
