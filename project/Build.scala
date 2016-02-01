import sbt._
import sbtbuildinfo.Plugin._
import play._
import Keys._

object ApplicationBuild extends Build {

  val appName         = "nsi-requester"
  val appVersion      = "1.1-SNAPSHOT"

  val appDependencies = Seq(
    "joda-time" % "joda-time" % "2.4",
    "com.typesafe.play" %% "play-ws" % "2.3.3"
  )

  lazy val gitHeadCommitSha = settingKey[String]("current git commit SHA")

  val headCommitSha = try { Process("git rev-parse --short HEAD").lines.head } catch { case ex: Exception => "undefined" }

  val main = Project(
      id = appName,
      base = file("."),
      settings = buildInfoSettings ++
        Seq(organization := "nl.surfnet.bod",
          scalaVersion := "2.11.7",
          scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
          gitHeadCommitSha := headCommitSha,
          sourceGenerators in Compile <+= buildInfo,
          buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, gitHeadCommitSha),
          buildInfoPackage := "support",
          libraryDependencies ++= appDependencies
        )).enablePlugins(PlayScala)
}
