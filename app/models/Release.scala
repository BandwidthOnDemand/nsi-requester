package models

import java.net.URI

case class Release(connectionId: String, correlationId: String, replyTo: Option[URI], requesterNsa: String, provider: Provider)
    extends NsiRequest(correlationId, replyTo, requesterNsa, provider) {

  override def nsiV2Body =
    <type:release>
      <connectionId>{ connectionId }</connectionId>
    </type:release>
}