package controllers

import play.api.test._
import play.api.data.FormError
import play.api.libs.json._
import support.WithViewContext

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class FormErrorWritesSpec extends support.Specification {

  "FormErrorWrites" should new WithViewContext {

    val subject = inject[controllers.ApplicationController]

    import subject.FormErrorWrites

    "write json for FormError" in {
      val error = FormError("reserve.connectionId", "required")

      val json = Json.toJson(error)

      (json \("id")).as[String] must equalTo("reserve_connectionId")
      (json \("message")).as[String] must equalTo("required")
    }

    "write json for a seq of FormError" in {
      val errors = Seq(FormError("reserve.connectionId", "required"), FormError("reserve.source", "required"))

      val json = Json.toJson(errors)

      json \\("id") must equalTo(List(JsString("reserve_connectionId"), JsString("reserve_source")))
    }
  }
}
