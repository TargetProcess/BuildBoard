package controllers

import play.api.libs.json._
import Writes._

object Branches extends Application {

  def branches = IsAuthorizedComponent {
    component =>
      implicit request =>
        val branches = component.branchRepository.getBranchInfos

        Ok(Json.toJson(branches))
  }
}
