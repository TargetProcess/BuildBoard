package models.cycles

import play.api.Play
import scala.util.Try
import scala.collection.JavaConverters._
import play.api.Play.current

abstract class ConfigurableCycle(val name: String) extends Cycle {
  val config = Play.configuration.getConfig(s"build.cycle.$name").get

  val unitTests = getTests(Cycle.unitTestsCategoryName)
  val funcTests = getTests(Cycle.funcTestsCategoryName)
  val casperJsTests = getTests(Cycle.casperCategoryName)
  val karmaJsTests = getTests(Cycle.karmaCategoryName)
  val pythonFuncTests = getTests(Cycle.pythonFuncTestsCategoryName)
  val includeUnstable = getBoolean("includeUnstable")
  val buildFullPackage = getBoolean("buildFullPackage")
  val includeComet = getBoolean("includeComet")
  val includeSlice = getBoolean("includeSlice")
  val includeCasper = getBoolean("includeCasper")
  val includeDb = getBoolean("includeDb")
  val isFull = getBoolean("isFull")
  val includePerfTests = getBoolean("includePerfTests")


  def getBoolean(path: String) = config.getBoolean(path).getOrElse(false)

  def getTests(path: String): String = {
    Try {
      config.getStringList(path).map(l => "\"" + l.asScala.mkString(" ") + "\"").get
    }.toOption
      .orElse(config.getString(path))
      .getOrElse("All")
  }
}
