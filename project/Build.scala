import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "BuildBoard"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    anorm,
    "se.radley" %% "play-plugins-salat" % "1.3.0")


  val main = play.Project(appName, appVersion, appDependencies).settings(
    libraryDependencies ++= Seq(
      "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.5",
      "org.scalaj" %% "scalaj-http" % "0.3.9",
      "com.offbytwo.jenkins" % "jenkins-client" % "0.2.0"),
    routesImport += "se.radley.plugin.salat.Binders._",
    templatesImport += "org.bson.types.ObjectId")

}
