package models

case class Ack(correlationId: String, requesterNsa: String, providerNsa: String)
  extends NsiRequest(correlationId, None, requesterNsa, providerNsa, protocolVersion = NsiRequest.NsiV2RequesterProtocolVersion) {

  override def nsiV2Body =
    <type:acknowledgment />

}