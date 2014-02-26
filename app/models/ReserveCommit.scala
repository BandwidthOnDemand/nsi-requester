package models

import java.net.URI

case class ReserveCommit(
    connectionId: String,
    correlationId: String,
    replyTo: Option[URI],
    requesterNsa: String,
    providerNsa: String) extends NsiRequest(correlationId, replyTo, requesterNsa, providerNsa) {

  override def nsiV2Body =
    <type:reserveCommit>
      <connectionId>{ connectionId }</connectionId>
    </type:reserveCommit>

}