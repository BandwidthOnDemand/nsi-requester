package models

import java.net.URI

case class Provider(
  providerUrl: URI,
  nsiVersion: Int,
  username: Option[String],
  password: Option[String],
  accessToken: Option[String])
