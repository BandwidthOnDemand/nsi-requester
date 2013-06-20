package models

import scala.xml.Null

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
  replyTo: String,
  nsaProvider: String) extends NsiRequest(correlationId, replyTo, nsaProvider) {

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
    { startNotificationId.map(id => <startNotificationId>{ id }</startNotificationId>).getOrElse(Null) },
    { endNotificationId.map(id => <endNotificationId>{ id }</endNotificationId>).getOrElse(Null) })


  override def nsiV1Body = sys.error("QueryNotification is not a supported NSI v1 operation")

}
