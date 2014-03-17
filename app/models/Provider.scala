package models

import java.net.URI

case class Provider(nsaId: String, providerUrl: URI, portPrefix: String)

case class EndPoint(provider: Provider, accessTokens: List[String])