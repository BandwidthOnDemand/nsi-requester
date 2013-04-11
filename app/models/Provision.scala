package models

case class Provision(connectionId: String, correlationId: String, replyTo: String, providerNsa: String)
    extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def nsiV2Body = ???

  override def nsiV1Body =
    <int:provisionRequest>
       { nsiRequestFields }
       <type:provision>
          { nsas }
          <connectionId>{ connectionId }</connectionId>
       </type:provision>
    </int:provisionRequest>

}
