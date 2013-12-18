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

    "respond with an NSI V1 Ok" in new WithApplication {
      val body =
        <Envelope>
          <header>
            <correlationId>{ "urn:uuid:1234" }</correlationId>
          </header>
          <Body>
            <ns:request xmlns:ns="http://schemas.ogf.org/nsi/2011/10/interface" />
          </Body>
        </Envelope>

      val result = ResponseController.reply()(FakeXmlRequest(body))

      contentAsString(result) must contain("http://schemas.ogf.org/nsi/2011/10")
      status(result) must equalTo(200)
    }

    "respond with an NSI V2 Ok" in new WithApplication {
      val body =
        <Envelope>
          <header>
            <correlationId>{ "urn:uuid:1234" }</correlationId>
          </header>
          <Body>
            <ns:request xmlns:ns="http://schemas.ogf.org/nsi/2013/12/interface" />
          </Body>
        </Envelope>

      val result = ResponseController.reply()(FakeXmlRequest(body))

      contentAsString(result) must contain("http://schemas.ogf.org/nsi/2013/12")
      contentAsString(result) must contain("<protocolVersion>application/vnd.ogf.nsi.cs.v2.requester+soap</protocolVersion>")
      status(result) must equalTo(200)
    }

    "respond with a BadRequest when the correlationId is missing" in new WithApplication {
      val body =
        <header>
          <noCorrelationId/>
        </header>

      val result = ResponseController.reply()(FakeXmlRequest(body))

      status(result) must equalTo(400)
    }

    "respond with a BadRequest when correlationId has not the correct form" in new WithApplication {
      val body =
        <header>
          <correlationId>{ "123-abc" }</correlationId>
        </header>

      val result = ResponseController.reply()(FakeXmlRequest(body))

      status(result) must equalTo(400)
    }
  }

  object FakeXmlRequest {
    def apply(body: scala.xml.Elem): FakeRequest[scala.xml.Elem] =
      FakeRequest("POST", "/", FakeHeaders(), body)
  }

}