package models

case class ReserveAbort(
    connectionId: String,
    correlationId: String,
    replyTo: String,
    providerNsa: String) extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def nsiV1SoapAction = ""
  override def nsiV2SoapAction = "http://schemas.ogf.org/nsi/2013/04/connection/service/reserveAbort"

  override def nsiV2Body =
    <type:reserveAbort>
      <connectionId>{ connectionId }</connectionId>
    </type:reserveAbort>

  override def nsiV1Body = sys.error("ReserveAbort is not a supported NSI v1 operation")
}