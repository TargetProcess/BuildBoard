package models


case class User(githubToken:Option[String], tpToken:Option[String], name:String)
