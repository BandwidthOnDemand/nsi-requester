package models

import org.junit.runner.RunWith
import org.specs2.mutable.Specification

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class QueryMessageSpec extends support.Specification with org.specs2.matcher.XmlMatchers {

  import QueryMessageMode._

  "QueryNotifications" should {

    "contain a start and end notification id" in {
      val queryNotifiation = QueryMessage(NotificationAsync, "connectionId", Some(2), Some(10), "correlationId", None, "nsaRequester", "nsaProvider")

      val body = queryNotifiation.nsiV2Body

      body must \\("queryNotification")
      body must \\("connectionId") \> "connectionId"
      body must \\("startNotificationId") \> "2"
      body must \\("endNotificationId") \> "10"
    }

    "be a sync query" in {
      val queryNotifiation = QueryMessage(NotificationSync, "connectionId", Some(2), Some(10), "correlationId", None, "nsaRequester", "nsaProvider")

      val body = queryNotifiation.nsiV2Body

      body must \\("queryNotificationSync")
    }

  }
}