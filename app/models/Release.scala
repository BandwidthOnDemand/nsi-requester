package models

import java.net.URI

case class Release(connectionId: String, correlationId: String, replyTo: Option[URI], providerNsa: String)
    extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def nsiV1SoapAction = ""
  override def nsiV2SoapAction = "http://schemas.ogf.org/nsi/2013/07/connection/service/release"

  override def nsiV2Body =
    <type:release>
      <connectionId>{ connectionId }</connectionId>
    </type:release>

  override def nsiV1Body =
    <int:releaseRequest>
      { nsiRequestFields }
      <type:release>
        { nsas }
        <connectionId>{ connectionId }</connectionId>
      </type:release>
    </int:releaseRequest>
}
