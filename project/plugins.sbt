// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.5")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")

addDependencyTreePlugin

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
