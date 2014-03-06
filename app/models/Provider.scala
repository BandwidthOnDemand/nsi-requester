package models

import java.net.URI

case class Provider(nsaId: String, providerUrl: URI, portPrefix: String, twoWayTls: Boolean)

case class EndPoint(provider: Provider, accessToken: Option[String])