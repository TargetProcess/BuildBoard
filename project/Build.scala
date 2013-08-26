import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "BuildBoard"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    libraryDependencies ++= Seq("org.kohsuke" % "github-api" % "1.43",
      "junit" % "junit" % "4.10" % "test",
      "org.scalaj" %% "scalaj-http" % "0.3.9",
      "com.offbytwo.jenkins" % "jenkins-client" % "0.1.4"
    )
  )

}
