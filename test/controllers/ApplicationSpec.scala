package controllers

import com.ning.http.client.Realm.AuthScheme
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.{ Json, JsObject }
import play.api.libs.ws.WS

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class ApplicationSpec extends support.Specification {

  "The application controller" should {

    "give a 400 if the url for validation is missing" in {
      val result = Application.validateProvider(FakeJsonRequest(Json.obj("foo" -> "bar")))

      status(result) must equalTo(400)
    }

    "give a 400 if the url for validation is empty" in {
      val result = Application.validateProvider(FakeJsonRequest(Json.obj("url" -> "")))

      status(result) must equalTo(400)
    }
  }

  "Adding Authorization" should {


    "add a authorization header containing the OAuth token" in {
      val holder = Application.addAuthenticationHeader(Some("token"))(WS.url("/"))

      holder.headers must havePair("Authorization" -> Seq("bearer token"))
      holder.auth must beNone
    }

    "add a no authorization header" in {
      val holder = Application.addAuthenticationHeader(None)(WS.url("/"))

      holder.headers must beEmpty
      holder.auth must beNone
    }
  }

  object FakeJsonRequest {
    def apply(body: JsObject): FakeRequest[JsObject] = FakeRequest("POST", "/", FakeHeaders(), body)
  }
}