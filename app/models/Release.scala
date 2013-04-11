package models

case class Release(connectionId: String, correlationId: String, replyTo: String, providerNsa: String)
    extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def nsiV2Body = ???

  override def nsiV1Body =
    <int:releaseRequest>
      { nsiRequestFields }
      <type:release>
        { nsas }
        <connectionId>{ connectionId }</connectionId>
      </type:release>
    </int:releaseRequest>
}
