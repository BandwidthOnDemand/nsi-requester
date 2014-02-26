package models

import java.net.URI

case class Terminate(
  connectionId: String,
  correlationId: String,
  replyTo: Option[URI],
  requesterNsa: String,
  providerNsa: String) extends NsiRequest(correlationId, replyTo, requesterNsa, providerNsa) {

  override def nsiV2Body =
    <type:terminate>
      <connectionId>{ connectionId }</connectionId>
    </type:terminate>
}