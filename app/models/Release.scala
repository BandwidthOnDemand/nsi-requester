package models

import java.net.URI

case class Release(connectionId: String, correlationId: String, replyTo: Option[URI], requesterNsa: String, providerNsa: String)
    extends NsiRequest(correlationId, replyTo, requesterNsa, providerNsa) {

  override def nsiV2Body =
    <type:release>
      <connectionId>{ connectionId }</connectionId>
    </type:release>
}