package controllers

object Landing extends Application {
  def index = {
    IsAuthorized {
      user =>
        implicit request => Ok(views.html.index(user))
    }
  }
}
