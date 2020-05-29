name := "BuildBoard"
version := "1.0"
scalaVersion := "2.11.11"

resolvers ++= Seq(
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases" at "http://oss.sonatype.org/content/repositories/releases",
  "java maven" at "https://maven.java.net/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "net.cloudinsights" %% "play-plugins-salat" % "1.5.9",
  "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.5",
  "org.scalaj" %% "scalaj-http" % "0.3.16" exclude("junit", "junit"),
  "com.netflix.rxjava" % "rxjava-scala" % "0.17.6",
  "com.github.nscala-time" %% "nscala-time" % "1.0.0",
  "org.specs2" %% "specs2" % "2.3.13",
  "org.mockito" % "mockito-all" % "1.9.5",
  "joda-time" % "joda-time" % "2.4",
  "net.java.dev.jna" % "jna" % "4.1.0",
  "net.java.dev.jna" % "jna-platform" % "4.1.0",
  filters
)

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions"
)

routesImport += "se.radley.plugin.salat.Binders._"
TwirlKeys.templateImports += "org.bson.types.ObjectId"

includeFilter in (Assets, LessKeys.less) := "*.less"
LessKeys.cleancss := true

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)