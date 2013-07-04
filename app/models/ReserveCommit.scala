package models

case class ReserveCommit(
    connectionId: String,
    correlationId: String,
    replyTo: String,
    providerNsa: String) extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def nsiV1SoapAction = ""
  override def nsiV2SoapAction = "http://schemas.ogf.org/nsi/2013/04/connection/service/reserveCommit"

  override def nsiV2Body =
    <type:reserveCommit>
      <connectionId>{ connectionId }</connectionId>
    </type:reserveCommit>

  override def nsiV1Body = sys.error("ReserveCommit is not a supported NSI v1 operation")

}