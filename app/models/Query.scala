package models

import java.net.URI

object QueryOperation extends Enumeration {
  type QueryOperation = Value
  val Summary, SummarySync, Details, Recursive = Value

  def operationsV1 = List(Summary, Details)
  def operationsV2 = List(Summary, SummarySync, Recursive)
}

import QueryOperation._

case class Query(
    operation: QueryOperation = Summary,
    connectionIds: List[String],
    globalReservationIds: List[String],
    correlationId: String,
    replyTo: Option[URI],
    requesterNsa: String,
    provider: Provider) extends NsiRequest(correlationId, replyTo, requesterNsa, provider) {

  override def soapAction() = {
    val action = operation match {
      case Summary => "querySummary"
      case SummarySync => "querySummarySync"
      case Recursive => "queryRecursive"
    }
   s"${NsiRequest.NsiV2SoapActionPrefix}/$action"
  }

  override def nsiV2Body = operation match {
    case Summary =>
      <type:querySummary>
        { connectionIdTags }
        { globalReservationIdTags }
      </type:querySummary>
    case SummarySync =>
      <type:querySummarySync>
        { connectionIdTags }
        { globalReservationIdTags }
      </type:querySummarySync>
    case Recursive =>
      <type:queryRecursive>
        { connectionIdTags }
        { globalReservationIdTags }
      </type:queryRecursive>
    case _ =>
      sys.error(s"Unsupported NSI v2 query type '$operation'")
  }

  private def connectionIdTags =
    connectionIds.map(id => <connectionId>{ id }</connectionId>)

  private def globalReservationIdTags =
    globalReservationIds.map(id => <globalReservationId>{ id }</globalReservationId>)

}
