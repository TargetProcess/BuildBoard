// Comment to get more information during initialization
logLevel := Level.Warn

scalaVersion := "2.10.4"

// The Typesafe repository 
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/" 

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.6")

//addSbtPlugin("com.github.mumoshu" % "play2-typescript" % "0.2-RC10")
// https://oss.sonatype.org/content/groups/public/com/github/mumoshu/play2-typescript_2.10_0.13/0.2-RC10/