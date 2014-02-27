package models

import java.net.URI

case class Provision(connectionId: String, correlationId: String, replyTo: Option[URI], requesterNsa: String, provider: Provider)
    extends NsiRequest(correlationId, replyTo, requesterNsa, provider) {

  override def nsiV2Body =
    <type:provision>
      <connectionId>{ connectionId }</connectionId>
    </type:provision>

}