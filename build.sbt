organization := "nl.surfnet.bod"
name := "nsi-requester"
version := "2.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.4"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Wunused:imports,privates,locals,params",
  "-release:21"
)

val gitHeadCommitSha = settingKey[String]("git HEAD SHA")
gitHeadCommitSha := scala.sys.process.Process("git rev-parse --short HEAD").lineStream.head

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, gitHeadCommitSha)
buildInfoPackage := "support"

val akkaVersion = "2.6.21"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

libraryDependencies ++= Seq(
  guice,
  ws,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "org.scala-stm" %% "scala-stm" % "0.11.1",
  "joda-time" % "joda-time" % "2.12.0",
  specs2 % "test",
  "org.specs2" %% "specs2-matcher-extra" % "4.20.7" % "test"
)

lazy val licenseText = settingKey[String]("Project license text.")

licenseText := IO.read(baseDirectory.value / "LICENSE")

organizationName := "SURFnet B.V."
startYear := Some(2012)
licenses += ("BSD-3-Clause", new URL("file:LICENSE"))

PlayKeys.devSettings += "play.server.websocket.periodic-keep-alive-max-idle" -> "10 seconds"
