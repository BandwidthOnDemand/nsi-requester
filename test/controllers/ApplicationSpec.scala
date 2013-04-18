package controllers

import org.specs2.mutable.Specification
import com.ning.http.client.Realm.AuthScheme
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.{ Json, JsObject }
import play.api.libs.ws.WS

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class ApplicationSpec extends Specification {

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

    "add no authorization header" in {
      val holder = Application.addAuthentication(Some(""), None, Some(""))(WS.url("/"))

      holder.headers must beEmpty
      holder.auth must beNone
    }

    "add a authorization header containing the OAuth token" in {
      val holder = Application.addAuthentication(None, None, Some("token"))(WS.url("/"))

      holder.headers must havePair("Authorization" -> Seq("bearer token"))
      holder.auth must beNone
    }

    "add a Basic Auth header" in {
      val holder = Application.addAuthentication(Some("user"), Some("pass"), None)(WS.url("/"))

      holder.headers must beEmpty
      holder.auth must beSome(("user", "pass", AuthScheme.BASIC))
    }
  }

  object FakeJsonRequest {
    def apply(body: JsObject): FakeRequest[JsObject] = FakeRequest("POST", "/", FakeHeaders(), body)
  }
}