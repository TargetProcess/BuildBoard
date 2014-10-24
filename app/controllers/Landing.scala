package controllers

import play.utils.UriEncoding.encodePathSegment

object Landing extends Application {
  def index = {
    IsAuthorized {
      user =>
        implicit request => Ok(views.html.index(user))
    }
  }

  def redirect(id: Int) = IsAuthorizedComponent {
    component =>
      implicit request => {
        val branch = component.branchRepository.getBranchByEntity(id)

        val url = routes.Landing.index().absoluteURL() + branch.map(b => s"#/list/branch?name=${encodePathSegment(b.name,"UTF-8")}&user=my&branch=all")
          .getOrElse("")

        Redirect(url)
      }
  }
}
