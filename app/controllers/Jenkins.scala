package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import com.github.nscala_time.time.Imports._
import Writes._
import models.jenkins.JenkinsRepository

object Jenkins extends Controller with Secured {

  def lastBuildInfo(branch: String) = IsAuthorized {
    implicit user =>
      request => Ok(Json.toJson(JenkinsRepository.getLastBuild(branch)))
  }

  def builds(branch: String) = IsAuthorized {
    implicit user =>
      request => Ok(Json.toJson(JenkinsRepository.getBuilds(branch)))
  }
}
