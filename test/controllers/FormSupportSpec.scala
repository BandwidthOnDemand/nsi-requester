package controllers

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import models.QueryNotificationOperation
import models.QueryOperation
import play.api.data.FormError

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class FormSupportSpec extends Specification {

  import FormSupport._

  "QueryNotificationOperationFormat" should {
    import QueryNotificationOperation._

    "parse Sync operation" in {
      val operation = queryNotificationOperationFormat.bind("operation", Map("operation" -> "Sync"))

      operation must beRight(QueryNotificationOperation.Sync)
    }

    "give formError when parsing 'asdfasfasfd' as an operation" in {
      val operation = queryNotificationOperationFormat.bind("operation", Map("operation" -> "asdfasdfasfd"))

      operation must beLeft(List(FormError("operation", "error.queryNotificationOperation", Nil)))
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