package controllers

import play.api.test.*
import play.api.libs.json.{Json, JsObject}
import support.WithViewContext

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class ApplicationControllerSpec extends support.Specification {

  "The application controller" should {

    "give a 400 if the url for validation is missing" in new WithViewContext {
      override def running() = {
        val subject = inject[controllers.ApplicationController]

        val result = subject.validateProvider(FakeJsonRequest(Json.obj("foo" -> "bar")))

        status(result) must equalTo(400)
      }
    }

    "give a 400 if the url for validation is empty" in new WithViewContext {
      override def running() = {
        val subject = inject[controllers.ApplicationController]

        val result = subject.validateProvider(FakeJsonRequest(Json.obj("url" -> "")))

        status(result) must equalTo(400)
      }
    }
  }

  object FakeJsonRequest {
    def apply(body: JsObject): FakeRequest[JsObject] = FakeRequest("POST", "/", FakeHeaders(), body)
  }
}
