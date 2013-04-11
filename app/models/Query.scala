package models

case class Query(
    operation: String = "Summary",
    connectionIds: List[String],
    globalReservationIds: List[String],
    correlationId: String,
    replyTo: String,
    nsaProvider: String) extends NsiRequest(correlationId, replyTo, nsaProvider) {

  override def nsiV2Body = operation match {
    case "Summary" =>
      <type:querySummary>
        { connectionIdTags }
        { globalReservationIdTags }
      </type:querySummary>
    case "SummarySync" =>
      <type:querySummarySync>
        { connectionIdTags }
        { globalReservationIdTags }
      </type:querySummarySync>
    case "Recursive" =>
      <type:queryRecursive>
        { connectionIdTags }
        { globalReservationIdTags }
      </type:queryRecursive>
    case _ =>
      sys.error(s"Unsupported NSI v2 query type '$operation'")
  }

  override def nsiV1Body =
    <int:queryRequest>
      { nsiRequestFields }
      <type:query>
        { nsas }
        <operation>{ operation }</operation>
        <queryFilter>
          { connectionIdTags }
          { globalReservationIdTags }
        </queryFilter>
      </type:query>
    </int:queryRequest>

  private def connectionIdTags =
    connectionIds.map(id => <connectionId>{ id }</connectionId>)

  private def globalReservationIdTags =
    globalReservationIds.map(id => <globalReservationId>{ id }</globalReservationId>)

}
