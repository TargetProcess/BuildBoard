package models.buildActions


import play.api.Play
import play.api.Play.current

import scala.collection.JavaConverters._
import scala.util.Try

object Cycle {
  val unitTestsCategoryName = "unitTests"
  val funcTestsCategoryName = "funcTests"
  val sliceCategoryName = "SliceLoadTests"
  val cometCategoryName = "CometTests"
  val casperCategoryName = "CasperTests"
  val dbCategoryName = "DbTests"
}

trait Cycle {
  val name: String
  val includeUnstable: Boolean
  val buildFullPackage: Boolean
  val unitTests: String
  val funcTests: String
  val includeComet: Boolean
  val includeSlice: Boolean
  val includeCasper: Boolean
  val includeDb: Boolean
  val isShortCycle: Boolean

  lazy val parameters = List(

    "IncludeUnitTests" -> unitTests,
    "IncludeFuncTests" -> funcTests,
    "BuildFullPackage" -> buildFullPackage.toString,
    "INCLUDE_UNSTABLE" -> includeUnstable.toString,
    "Cycle" -> (if (isShortCycle) "Short" else "Full"),
    "INCLUDE_COMET" -> includeComet.toString,
    "INCLUDE_SLICE" -> includeSlice.toString,
    "INCLUDE_CASPER" -> includeCasper.toString,
    "INCLUDE_DB" -> includeDb.toString
  )
}

class ConfigurableCycle(val name: String) extends Cycle {
  val config = Play.configuration.getConfig(s"build.cycle.$name").get

  override val unitTests = getTests(Cycle.unitTestsCategoryName)
  override val funcTests = getTests(Cycle.funcTestsCategoryName)

  override val includeUnstable = get("includeUnstable")
  override val includeComet: Boolean = get("includeComet")
  override val includeCasper: Boolean = get("includeCasper")
  override val includeDb: Boolean = get("includeDb")
  override val isShortCycle: Boolean = get("isShortCycle")
  override val includeSlice: Boolean = get("includeSlice")
  override val buildFullPackage = get("buildFullPackage")

  def get(path: String) = config.getBoolean(path).getOrElse(false)

  def getTests(path: String): String = {
    Try {
      config.getStringList(path).map(l => "\"" + l.asScala.mkString(" ") + "\"").get
    }.toOption
      .orElse(config.getString(path))
      .getOrElse("All")
  }
}

case object FullCycle extends ConfigurableCycle("Full")


case object ShortCycle extends ConfigurableCycle("Short")

case object BuildPackageOnly extends ConfigurableCycle("Package only")
