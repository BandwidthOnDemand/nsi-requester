package models

import java.net.URI

case class ReserveCommit(
    connectionId: String,
    correlationId: String,
    replyTo: Option[URI],
    requesterNsa: String,
    provider: Provider) extends NsiRequest(correlationId, replyTo, requesterNsa, provider) {

  override def nsiV2Body =
    <type:reserveCommit>
      <connectionId>{ connectionId }</connectionId>
    </type:reserveCommit>

}