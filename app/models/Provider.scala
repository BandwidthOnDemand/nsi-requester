package models

import java.net.URI

sealed trait NsiVersion {
  def value: Int

  def fold[X](v1: => X, v2: => X) = this match {
    case NsiVersion.V1 => v1
    case NsiVersion.V2 => v2
  }
}
object NsiVersion {
  def fromInt(n: Int) = n match {
    case 1 => V1
    case 2 => V2
    case n => sys.error(s"unrecognized NSI version $n")
  }
  case object V1 extends NsiVersion {
    val value = 1
    override def toString = "v1"
  }
  case object V2 extends NsiVersion {
    val value = 2
    override def toString = "v2"
  }
}

case class Provider(
  providerUrl: URI,
  nsiVersion: NsiVersion,
  username: Option[String],
  password: Option[String],
  accessToken: Option[String])
