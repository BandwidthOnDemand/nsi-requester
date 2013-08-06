package models

import java.net.URI

case class Terminate(connectionId: String, correlationId: String, replyTo: Option[URI], providerNsa: String)
    extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def nsiV1SoapAction = ""
  override def nsiV2SoapAction = "http://schemas.ogf.org/nsi/2013/07/connection/service/terminate"

  override def nsiV2Body =
    <type:terminate>
      <connectionId>{ connectionId }</connectionId>
    </type:terminate>

  override def nsiV1Body =
    <int:terminateRequest>
      { nsiRequestFields }
      <type:terminate>
        { nsas }
        <connectionId>{ connectionId }</connectionId>
      </type:terminate>
    </int:terminateRequest>
}
