package controllers

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import models.QueryMessageMode
import models.QueryOperation
import play.api.data.FormError

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class FormSupportSpec extends Specification {

  import FormSupport._

  "QueryNotificationOperationFormat" should {
    import QueryMessageMode._

    "parse Sync operation" in {
      val operation = queryMessageModeFormat.bind("operation", Map("operation" -> "NotificationSync"))

      operation must beRight(QueryMessageMode.NotificationSync)
    }

    "give formError when parsing 'asdfasfasfd' as an operation" in {
      val operation = queryMessageModeFormat.bind("operation", Map("operation" -> "asdfasdfasfd"))

      operation must beLeft(List(FormError("operation", "error.queryMessageMode", Nil)))
    }
  }

  "QueryOperationFormat" should {
    import QueryOperation._

    "parse Summary operation" in {
      val operation = queryOperationFormat.bind("operation", Map("operation" -> "Summary"))

      operation must beRight(QueryOperation.Summary)
    }
  }
}