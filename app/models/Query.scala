package models

case class Query(
    operation: String = "Summary",
    connectionIds: List[String],
    globalReservationIds: List[String],
    correlationId: String,
    replyTo: String,
    nsaProvider: String)
    extends NsiRequest(correlationId, replyTo, nsaProvider) {

  override def toNsiV2Envelope = ???

  override def toNsiV1Envelope = wrapNsiV1Envelope(
    <int:queryRequest>
      { nsiRequestFields }
      <type:query>
        { nsas }
        <operation>{ operation }</operation>
        <queryFilter>
          { connectionIds.map(id => <connectionId>{ id }</connectionId>) }
          { globalReservationIds.map(id => <globalReservationId>{ id }</globalReservationId>) }
        </queryFilter>
      </type:query>
    </int:queryRequest>
  )
}
