package models

case class Terminate(connectionId: String, correlationId: String, replyTo: String, providerNsa: String)
    extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def nsiV2Body = ???

  override def nsiV1Body =
    <int:terminateRequest>
      { nsiRequestFields }
      <type:terminate>
        { nsas }
        <connectionId>{ connectionId }</connectionId>
      </type:terminate>
    </int:terminateRequest>
}
