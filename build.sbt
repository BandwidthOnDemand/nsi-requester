organization := "nl.surfnet.bod"
name := "nsi-requester"
version := "1.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"
scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Xlint")

val gitHeadCommitSha = settingKey[String]("git HEAD SHA")
gitHeadCommitSha := Process("git rev-parse --short HEAD").lines.head

buildInfoSettings
sourceGenerators in Compile <+= buildInfo
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, gitHeadCommitSha)
buildInfoPackage := "support"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.4",
  "com.typesafe.play" %% "play-ws" % "2.3.10"
)

net.virtualvoid.sbt.graph.Plugin.graphSettings
