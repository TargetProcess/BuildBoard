package components

import models.User

trait LoggedUserProviderComponent {
  val loggedUser:Option[User]
}
