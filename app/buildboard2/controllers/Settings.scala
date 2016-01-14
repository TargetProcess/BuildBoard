package buildboard2.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

object Settings extends Controller {
  def settings = Action {
    implicit request =>
      Ok(Json.toJson(Map(
        "settings" -> Json.toJson(Map(
          "user" -> Map(
            "caption" -> "TP user",
            "type" -> "string",
            "id" -> "user"
          ),
          "token" -> Map(
            "caption" -> "TP auth token",
            "type" -> "string",
            "id" -> "token"
          )
        )),
        "methods" -> Json.toJson(Map(
          "builds" -> Json.toJson(Map(
            "get" -> Map.empty[String, String]
          )),
          "jobs" -> Json.toJson(Map(
            "get" -> Map.empty[String, String]
          ))
        ))
      )))
  }
}
