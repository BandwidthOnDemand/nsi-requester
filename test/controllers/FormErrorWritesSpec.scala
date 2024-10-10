package controllers

import play.api.data.FormError
import play.api.libs.json.*
import support.WithViewContext

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class FormErrorWritesSpec extends support.Specification:

  "FormErrorWrites" should {

    "write json for FormError" in new WithViewContext:
      override def running() =
        val subject = inject[controllers.ApplicationController]
        import subject.FormErrorWrites

        val error = FormError("reserve.connectionId", "required")

        val json = Json.toJson(error)

        (json \ ("id")).as[String] must equalTo("reserve_connectionId")
        (json \ ("message")).as[String] must equalTo("required")

    "write json for a seq of FormError" in new WithViewContext:
      override def running() =
        val subject = inject[controllers.ApplicationController]
        import subject.FormErrorWrites

        val errors =
          Seq(
            FormError("reserve.connectionId", "required"),
            FormError("reserve.source", "required")
          )

        val json = Json.toJson(errors)

        json \\ ("id") must equalTo(
          List(JsString("reserve_connectionId"), JsString("reserve_source"))
        )
  }
end FormErrorWritesSpec
