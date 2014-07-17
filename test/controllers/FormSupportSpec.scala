package controllers

import models.QueryMessageMode._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import models.QueryMessageMode
import models.QueryOperation
import play.api.data.FormError

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class FormSupportSpec extends Specification {

  import FormSupport._

  "QueryNotificationOperationFormat" should {

    "parse Sync operation" in {
      val operation = queryMessageModeFormat.bind("operation", Map("operation" -> "NotificationSync"))

      operation must beRight(QueryMessageMode.NotificationSync)
    }

    "give formError when parsing 'asdfasfasfd' as an operation" in {
      val operation = queryMessageModeFormat.bind("operation", Map("operation" -> "asdfasdfasfd"))

      println(operation)
      operation match {
        case Left(s) => {
          val formError = s.head
          println("------ Found LEFT key=" + formError.key + ", message=" + formError.message)
        }
        case Right(i) => println("----- Found RIGHT " + i)
      }

      val test : Either[Seq[FormError], QueryMessageMode] = Left(List(FormError("operation", "error.queryMessageMode", List())))

      test match {
        case Left(s) => {
          val formError = s.head
          println("------ Found LEFT key=" + formError.key + ", message=" + formError.message)
        }
        case Right(i) => println("----- Found RIGHT " + i)
      }

      if (operation == test) println("+++++ EQUAL!")

      operation must beLeft(List(FormError("operation", "error.queryMessageMode", List())))
    }
  }

  "QueryOperationFormat" should {

    "parse Summary operation" in {
      val operation = queryOperationFormat.bind("operation", Map("operation" -> "Summary"))

      println("2222222 " + operation);
      operation must beRight(QueryOperation.Summary)
    }
  }
}
