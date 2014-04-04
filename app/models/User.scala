package models

import se.radley.plugin.salat.Binders._

trait Login {
  val username: String
  val token: String
}

trait AuthInfo {
  val token: String
  val githubToken: String
}


case class User(
  id: ObjectId = new ObjectId,

  tpId: Int,

  username: String,
  token: String,

  githubLogin: String = null,
  githubToken: String = null,
  fullName: String = null) extends Login with AuthInfo


case class TpUser(id: Int, login: String, fullName: String)

