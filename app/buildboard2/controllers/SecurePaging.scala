package buildboard2.controllers

import buildboard2.Writes2._
import buildboard2.components.DefaultComponent
import play.api.libs.json._
import play.api.mvc._

trait SecurePaging {
  this: SecureAuthentication =>

  def SecurePage[A](take: Option[Int], skip: Option[Int], token: String)(f: => DefaultComponent => Page[A])(implicit writes: Writes[A]) = ToolTokenAuthenticatedComponent(token) {
    component =>
      implicit request =>
        val tk = take.getOrElse(1000)
        val skp = skip.getOrElse(0)
        val next = skp + tk

        val page = f(component)
        val nextPageUrl = if (next < page.count) Some(routes.Builds.builds(Some(tk), Some(next), token).absoluteURL()) else None

        Results.Ok(Json.toJson(PageResult(page.data.slice(skp, next).toList, nextPageUrl)))
  }
}

case class Page[A](data: Iterator[A], count: Long)

case class PageResult[A](items: List[A], next: Option[String])
