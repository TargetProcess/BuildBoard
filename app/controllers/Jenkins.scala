package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.jenkins._
import com.github.nscala_time.time.Imports._

object Jenkins extends Controller with Secured {
  implicit val buildWrite = Json.writes[Build]

  def lastBuildInfo(branch:String) = IsAuthorized {
    implicit user =>
      request =>  Ok(Json.toJson(Build(DateTime.now, if (branch.length %2 == 0)"success" else "danger")))

  }

}
