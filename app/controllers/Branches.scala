package controllers

import controllers.Writes._
import play.api.libs.json._

object Branches extends Application {

  def branchesWithLastBuilds = IsAuthorizedComponent {
    component =>
      implicit request =>
        Ok(Json.toJson(component.branchRepository.getBranchesWithLastBuild))
  }

  def branch(name:String) = IsAuthorizedComponent {
    component =>
      implicit request =>
        Ok(Json.toJson(component.branchRepository.getBranch(name)))
  }
}
