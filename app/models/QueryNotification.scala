package models

import java.net.URI
import scala.xml.NodeSeq.Empty

object QueryNotificationOperation extends Enumeration {
  type QueryNotificationOperation = Value
  val Sync, Async = Value
}

import QueryNotificationOperation._

case class QueryNotification(
  operation: QueryNotificationOperation,
  connectionId: String,
  startNotificationId: Option[Int],
  endNotificationId: Option[Int],
  correlationId: String,
  replyTo: Option[URI],
  nsaProvider: String) extends NsiRequest(correlationId, replyTo, nsaProvider) {

  override def nsiV1SoapAction = sys.error("QueryNotification is not a supported NSI v1 operation")
  override def nsiV2SoapAction = {
    val action = operation match {
      case Sync => "queryNotificationSync"
      case Async => "queryNotification"
    }
    s"http://schemas.ogf.org/nsi/2013/04/connection/service/$action"
  }

  override def nsiV2Body = operation match {
    case Async =>
      <type:queryNotification>
        { queryBody }
      </type:queryNotification>
    case Sync =>
      <type:queryNotificationSync>
        { queryBody }
      </type:queryNotificationSync>
  }

  private def queryBody = List(
    <connectionId>{ connectionId }</connectionId>,
    { startNotificationId.map(id => <startNotificationId>{ id }</startNotificationId>).getOrElse(Empty) },
    { endNotificationId.map(id => <endNotificationId>{ id }</endNotificationId>).getOrElse(Empty) })


  override def nsiV1Body = sys.error("QueryNotification is not a supported NSI v1 operation")

}
