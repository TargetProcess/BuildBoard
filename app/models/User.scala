package models


case class User(login:String, password:String)

object User { 
  def findByEmail(email:String):Option[User] = Some(User(email, "12345"))
}