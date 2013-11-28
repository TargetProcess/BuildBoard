package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.jenkins._
import com.github.nscala_time.time.Imports._
import Writes._

object Jenkins extends Controller with Secured {

  def lastBuildInfo(branch:String) = IsAuthorized {
    implicit user =>
      request =>  Ok(Json.toJson(Build(if (branch.length %2 == 0)"success" else "danger", "http://localhost:9000", DateTime.now, null)))

  }

}
