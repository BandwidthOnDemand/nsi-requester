package models

case class Provider(
  providerUrl: String,
  nsiVersion: Int,
  username: Option[String],
  password: Option[String],
  accessToken: Option[String])
