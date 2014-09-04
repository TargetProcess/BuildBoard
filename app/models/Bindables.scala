package models

import play.api.mvc.QueryStringBindable

/**
 * Created by Truhtanov on 9/3/2014.
 */
class Bindables {
  implicit def binder(implicit stringBinder: QueryStringBindable[String], listBinder: QueryStringBindable[List[String]]) = new QueryStringBindable[BuildParametersCategory] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, BuildParametersCategory]] = {
      for {
        name <- stringBinder.bind(key + ".name", params)
        parts <- listBinder.bind(key + ".parts", params)
      } yield {
        (name, parts) match {
          case (Right(name), Right(parts)) => Right(BuildParametersCategory(name, parts))
          case _ => Left("Unable to bind BuildParametersCategory")
        }
      }
    }

    def unbind(key: String, category: BuildParametersCategory) =
      stringBinder.unbind(key + ".name", category.name) + "&" + listBinder.unbind(key + ".size", category.parts)
  }
}
