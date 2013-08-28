package models.tp

import models._
import models.Login
import scalaj.http._
import TargetprocessApplication._
import scala.util._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

object test {

	implicit val user = UserCredentials(username="fomin",password="123456")
                                                  //> user  : models.UserCredentials = UserCredentials(fomin,123456)
	
	val t = EntityRepo.getAssignables(List(100,122))
                                                  //> t  : scala.util.Try[models.tp.Assignable] = Failure(play.api.libs.json.JsRes
                                                  //| ultException: JsResultException(errors:List((/Id,List(ValidationError(valida
                                                  //| te.error.missing-path,WrappedArray()))), (/EntityState,List(ValidationError(
                                                  //| validate.error.missing-path,WrappedArray()))), (/Name,List(ValidationError(v
                                                  //| alidate.error.missing-path,WrappedArray()))))))
	  

	
   
}