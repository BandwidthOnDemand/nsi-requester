import com.typesafe.sbt.less.Import.LessKeys
import sbt._
import sbtbuildinfo.Plugin._
import play._

import Keys._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName         = "nsi-requester"
  val appVersion      = "1.1-SNAPSHOT"

  val appDependencies = Seq(
    "joda-time" % "joda-time" % "2.3",
    "com.typesafe.play" %% "play-ws" % "2.3.1"
  )

  lazy val gitHeadCommitSha = settingKey[String]("current git commit SHA")

  val main = Project(
      id = appName,
      base = file("."),
    settings =
      buildInfoSettings ++
      Seq(organization := "nl.surfnet.bod",
        scalaVersion := "2.10.4",
        scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
        gitHeadCommitSha := Process("git rev-parse --short HEAD").lines.head,
        sourceGenerators in Compile <+= buildInfo,
        buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, gitHeadCommitSha),
        buildInfoPackage := "support",
        libraryDependencies ++= appDependencies
      )).enablePlugins(PlayScala)
}
