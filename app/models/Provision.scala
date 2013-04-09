package models

case class Provision(connectionId: String, correlationId: String, replyTo: String, providerNsa: String)
    extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def toNsiV2Envelope = ???

  override def toNsiV1Envelope = wrapNsiV1Envelope(
    <int:provisionRequest>
       { nsiRequestFields }
       <type:provision>
          { nsas }
          <connectionId>{ connectionId }</connectionId>
       </type:provision>
    </int:provisionRequest>
  )

}
