// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/" 

// Use the Play sbt plugin for Play projects + Less for less => CSS.
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.10")
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")

//addSbtPlugin("com.github.mumoshu" % "play2-typescript" % "0.2-RC10")
// https://oss.sonatype.org/content/groups/public/com/github/mumoshu/play2-typescript_2.10_0.13/0.2-RC10/