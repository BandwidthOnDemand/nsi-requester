import sbt._
import Keys._
import play.Project._
import sbtbuildinfo.Plugin._

object ApplicationBuild extends Build {

  val appName         = "nsi-requester"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "joda-time" % "joda-time" % "2.1"
  )

  lazy val gitHeadCommitSha = settingKey[String]("current git commit SHA")

  val main = play.Project(appName, appVersion, appDependencies,
    settings = play.Project.playScalaSettings ++ buildInfoSettings
  ).settings(
    organization := "nl.surfnet.bod",
    scalaVersion := "2.10.4",
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),

    gitHeadCommitSha := Process("git rev-parse --short HEAD").lines.head,
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, gitHeadCommitSha),
    buildInfoPackage := "support"
  )

}
