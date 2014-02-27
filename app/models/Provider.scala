package models

import java.net.URI

case class Provider(nsaId: String, providerUrl: URI)

case class EndPoint(provider: Provider, accessToken: Option[String])