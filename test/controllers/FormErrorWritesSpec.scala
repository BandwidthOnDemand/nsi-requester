package controllers

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import play.api.data.FormError
import play.api.libs.json.Json
import play.api.libs.json.JsString
import play.api.libs.json.JsArray

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class FormErrorWritesSpec extends Specification {

  "FormErrorWrites" should {

    import Application.FormErrorWrites

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