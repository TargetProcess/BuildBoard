package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.jenkins._
import com.github.nscala_time.time.Imports._
import Writes._

object Jenkins extends Controller with Secured {

  def generateBuild(branch:String, day:Int=0) =
    Build(if (branch.length %(1+day) == 0)"success" else "danger", s"http://localhost:9000/$day", DateTime.now-day.days, null)

  def lastBuildInfo(branch:String) = IsAuthorized {
    implicit user =>


      request =>{
        val build = generateBuild(branch)
        Ok(Json.toJson(build))
      }

  }

  def builds(branch:String) = IsAuthorized {
    implicit user =>
      request =>  Ok(Json.toJson(
        (0 to 5).map(generateBuild(branch, _))
      ))
  }

}
