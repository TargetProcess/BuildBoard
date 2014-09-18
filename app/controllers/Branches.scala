package controllers

import controllers.Writes._
import models.branches.Branch
import play.api.libs.json._

object Branches extends Application {

  def branches = IsAuthorizedComponent {
    component =>
      implicit request =>
        Ok(Json.toJson(component.branchRepository.getBranchesWithLastBuild))
  }


  def branches1 = IsAuthorizedComponent {
    component =>
      implicit request =>
        val branches: List[Branch] = component.branchRepository.getBranches

        Ok(Json.toJson(branches.map(b => b.copy(
          activity = Nil,
          lastBuild = b.lastBuild.map(_.copy(
            commits = Nil,
            node = None))
        ))))
  }
}
