package components

import models.AuthInfo

trait AuthInfoProviderComponent {
  def authInfo: AuthInfo
}
