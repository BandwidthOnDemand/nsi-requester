package models

case class Query(connectionIds: List[String], globalReservationIds: List[String], correlationId: String, replyTo: String, nsaProvider:
  String)
    extends NsiRequest(correlationId, replyTo, nsaProvider) {

  def toEnvelope = inEnvelope(
    <int:queryRequest>
      { nsiRequestFields }
      <type:query>
        { nsas }
        <operation>Summary</operation>
        <queryFilter>
          { eitherConnectionIdsOrGlobalReservationIds }
        </queryFilter>
      </type:query>
    </int:queryRequest>
  )

  def eitherConnectionIdsOrGlobalReservationIds = {
    connectionIds match {
      case Nil => globalReservationIds.map(id => <globalReservationId>{ id }</globalReservationId>)
      case _ => connectionIds.map(id => <connectionId>{ id }</connectionId>)
    }
  }
}
