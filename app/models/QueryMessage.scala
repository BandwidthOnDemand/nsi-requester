package models

import java.net.URI
import scala.xml.NodeSeq.Empty

object QueryMessageMode extends Enumeration {
  type QueryMessageMode = Value
  val NotificationSync, NotificationAsync, ResultSync, ResultAsync = Value
}

import QueryMessageMode._

case class QueryMessage(
  operation: QueryMessageMode,
  connectionId: String,
  startId: Option[Long],
  endId: Option[Long],
  correlationId: String,
  replyTo: Option[URI],
  requesterNsa: String,
  provider: Provider) extends NsiRequest(correlationId, replyTo, requesterNsa, provider) {

  override def soapAction = {
    val action = operation match {
      case NotificationSync => "queryNotificationSync"
      case NotificationAsync => "queryNotification"
      case ResultSync => "queryResultSync"
      case ResultAsync => "queryResult"
    }
    s"${NsiRequest.NsiV2SoapActionPrefix}/$action"
  }

  override def nsiV2Body =
    operation match {
      case NotificationAsync =>
        <type:queryNotification>
           { queryBody }
         </type:queryNotification>
      case NotificationSync =>
        <type:queryNotificationSync>
           { queryBody }
        </type:queryNotificationSync>
      case ResultAsync =>
        <type:queryResult>
           { queryBody }
        </type:queryResult>
      case ResultSync =>
        <type:queryResultSync>
           { queryBody }
        </type:queryResultSync>
  }

  private def queryBody = List(
    <connectionId>{ connectionId }</connectionId>,
    { startIdTag },
    { endIdTag }
  )

  private def startIdTag = operation match {
    case NotificationSync | NotificationAsync => startId.map(id => <startNotificationId>{ id }</startNotificationId>).getOrElse(Empty)
    case ResultSync | ResultAsync             => startId.map(id => <startResultId>{ id }</startResultId>).getOrElse(Empty)
  }

  private def endIdTag = operation match {
    case NotificationSync | NotificationAsync => endId.map(id => <endNotificationId>{ id }</endNotificationId>).getOrElse(Empty)
    case ResultSync | ResultAsync             => endId.map(id => <endResultId>{ id }</endResultId>).getOrElse(Empty)
  }

}