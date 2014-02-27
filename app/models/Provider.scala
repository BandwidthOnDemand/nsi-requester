package models

import java.net.URI

case class Provider(nsaId: String, providerUrl: URI)

case class EndPoint(provider: Provider, accessToken: Option[String])

object Provider {
  def all: List[Provider] =
    Provider("urn:ogf:network:surfnet.nl:1990:nsa:bod-dev", URI.create("http://localhost:8082/bod/nsi/v2/provider")) ::
    Provider("urn:bod", URI.create("hptt://localhost:8080")) :: Nil

  def find(nsaId: String) = all.find(_.nsaId == nsaId)
}