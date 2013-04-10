package controllers

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import play.api.test._
import play.api.test.Helpers._
import play.api.http.HeaderNames.CONTENT_TYPE
import org.specs2.matcher.BeEqualTo

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class ResponseControllerSpec extends Specification {

  "The response controllers" should {

    "respond with an Ok when the correlationId is present" in new WithApplication {
      val body =
        <header>
          <correlationId>{ "urn:uuid:1234" }</correlationId>
        </header>

      val result = ResponseController.reply()(FakeRequest("POST", "/", FakeHeaders(), body = body))

      status(result) must equalTo(200)
    }

    "respond with a BadRequest when the correlationId is missing" in new WithApplication {
      val body =
        <header>
          <noCorrelationId/>
        </header>

      val result = ResponseController.reply()(FakeRequest("POST", "/", FakeHeaders(), body = body))

      status(result) must equalTo(400)
    }

    "respond with a BadRequest when correlationId has not the correct form" in new WithApplication {
      val body =
        <header>
          <correlationId>{ "123-abc" }</correlationId>
        </header>

      val result = ResponseController.reply()(FakeRequest("POST", "/", FakeHeaders(), body = body))

      status(result) must equalTo(400)
    }
  }

}