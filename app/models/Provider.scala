package models

case class Provider(providerUrl: String, username: Option[String], password: Option[String], accessToken: Option[String])
