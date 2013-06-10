import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "nsi-requester"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "joda-time" % "joda-time" % "2.1"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalaVersion := "2.10.2",
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Xlint")
  )

}
