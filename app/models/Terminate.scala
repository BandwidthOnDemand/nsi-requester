package models

case class Terminate(connectionId: String, correlationId: String, replyTo: String, providerNsa: String)
    extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def toNsiV2Envelope = ???

  override def toNsiV1Envelope = wrapNsiV1Envelope(
    <int:terminateRequest>
      { nsiRequestFields }
      <type:terminate>
        { nsas }
        <connectionId>{ connectionId }</connectionId>
      </type:terminate>
    </int:terminateRequest>
  )
}
