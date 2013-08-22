package models

case class Ack(correlationId: String, providerNsa: String) extends NsiRequest(correlationId, None, providerNsa) {

  override def nsiV2Body =
    <type:acknowledgment />

  override def nsiV1Body =
    <int:acknowledgment>
      <int:correlationId>{ "urn:uuid:" + correlationId }</int:correlationId>
    </int:acknowledgment>
}