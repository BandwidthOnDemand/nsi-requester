package models

case class Provision(connectionId: String, correlationId: String, replyTo: String, providerNsa: String)
    extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def nsiV1SoapAction = ""
  override def nsiV2SoapAction = "http://schemas.ogf.org/nsi/2013/04/connection/service/provision"

  override def nsiV2Body =
    <type:provision>
      <connectionId>{ connectionId }</connectionId>
    </type:provision>

  override def nsiV1Body =
    <int:provisionRequest>
       { nsiRequestFields }
       <type:provision>
          { nsas }
          <connectionId>{ connectionId }</connectionId>
       </type:provision>
    </int:provisionRequest>

}
