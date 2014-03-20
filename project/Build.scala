import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "BuildBoard"
  val appVersion = "1.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    anorm,
    "se.radley" %% "play-plugins-salat" % "1.3.0")


  val main = play.Project(appName, appVersion, appDependencies).settings(
    libraryDependencies ++= Seq(
      "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.5",
      "org.scalaj" %% "scalaj-http" % "0.3.9" exclude("junit", "junit"),
      "com.github.nscala-time" %% "nscala-time" % "0.6.0",
      "com.netflix.rxjava" % "rxjava-scala" % "0.15.0",
      "com.github.nscala-time" %% "nscala-time" % "0.8.0"
    ),
    routesImport += "se.radley.plugin.salat.Binders._",
    templatesImport += "org.bson.types.ObjectId"
//     ,javascriptEntryPoints <<= baseDirectory(base => base / "app" / "assets" ** "*.js"))
  )

}
