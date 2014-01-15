package models

import java.net.URI
import scala.xml.NodeSeq.Empty
import models.QueryOperationMode._

case class QueryResult(
  operation: QueryOperationMode,
  connectionId: String,
  startResultId: Option[Long],
  endResultId: Option[Long],
  correlationId: String,
  replyTo: Option[URI],
  requesterNsa: String,
  providerNsa: String) extends NsiRequest(correlationId, replyTo, requesterNsa, providerNsa) {

  override def nsiV1SoapAction = sys.error("QueryResult is not a supported NSI v1 operation")
  override def nsiV2SoapAction = {
    val action = operation match {
      case Sync => "queryResultSync"
      case Async => "queryResult"
    }
    s"${NsiRequest.NsiV2SoapActionPrefix}/$action"
  }

  override def nsiV2Body = operation match {
    case Async =>
      <type:queryResult>
        { queryBody }
      </type:queryResult>
    case Sync =>
      <type:queryResultSync>
        { queryBody }
      </type:queryResultSync>
  }

  private def queryBody = List(
    <connectionId>{ connectionId }</connectionId>,
    { startResultId.map(id => <startResultId>{ id }</startResultId>).getOrElse(Empty) },
    { endResultId.map(id => <endResultId>{ id }</endResultId>).getOrElse(Empty) })


  override def nsiV1Body = sys.error("QueryResult is not a supported NSI v1 operation")

}
