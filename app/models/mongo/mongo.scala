package models.mongo

import com.novus.salat.{TypeHintFrequency, Context}
import play.api.Play
import play.api.Play.current
import com.novus.salat.dao.{SalatDAO, ModelCompanion}
import models._
import se.radley.plugin.salat._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat.Binders.ObjectId
import models.Branch
import com.novus.salat.StringTypeHintStrategy
import models.Build
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers

object mongoContext {
  implicit val context = {
    val context = new Context {
      val name = "global"
      override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    }
    context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
    context.registerClassLoader(Play.classloader)
    RegisterJodaTimeConversionHelpers()

    context
  }
}

