package controllers

import play.api.libs.json._

object Branches extends Application {

  def branches = IsAuthorizedComponent {
    component =>
      implicit request =>
        val branches = component.branchRepository.getBranches
        import controllers.Writes._

        Ok(Json.toJson(branches.toList))
  }
}
