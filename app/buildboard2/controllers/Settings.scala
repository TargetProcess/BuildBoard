package buildboard2.controllers

import play.api.libs.json.Json
import play.api.mvc.{Controller, Action}
import buildboard2.controllers.Writes2._

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
            "get" -> Map(
              "params" -> Map(
                "build.id" -> Map(
                  "required" -> Json.toJson(true)
                )
              )
            )
          ))
        ))
      )))
  }
}
