package controllers

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import models.QueryNotificationOperation
import play.api.data.FormError

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class FormSupportSpec extends Specification {

  import QueryNotificationOperation._

  "QueryNotificationOperationFormat" should {

    "parse sync operation" in {
      val operation = FormSupport.queryNotificationOperationFormat.bind("operation", Map("operation" -> "Sync"))

      operation must beRight(QueryNotificationOperation.Sync)
    }

    "give formError when parsing 'asdfasfasfd' as an operation" in {
      val operation = FormSupport.queryNotificationOperationFormat.bind("operation", Map("operation" -> "asdfasdfasfd"))

      operation must beLeft(List(FormError("operation", "error.queryNotificationOperation", Nil)))
    }
  }
}