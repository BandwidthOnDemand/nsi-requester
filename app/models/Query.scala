package models

case class Query(correlationId: String, connectionIds: List[String], globalReservationIds: List[String]) extends Soapable {

  def toEnvelope(replyTo: String) = inEnvelope(
    <int:queryRequest>
      <int:correlationId>{ correlationId }</int:correlationId>
      <int:replyTo>{ replyTo }</int:replyTo>
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
