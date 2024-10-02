package models

import java.util.Date

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class ReserveSpec extends support.Specification with org.specs2.matcher.XmlMatchers {

  "NSI reserve" should {

    "have a NSI header element with a connection trace" in {
      val res = DefaultReservation().copy(correlationId = "FD5C4151-F980-410A-8565-5E8EDCE880F1")

      val envelope = res.toNsiEnvelope()
      envelope must \("Header") \ ("nsiHeader") \ ("correlationId") \>
        "urn:uuid:FD5C4151-F980-410A-8565-5E8EDCE880F1"
      envelope must \("Header") \ ("nsiHeader") \ ("ConnectionTrace") \ ("Connection")
    }

    "have a source and destination STP id" in {
      val res =
        DefaultReservation().copy(source = Port("source"), destination = Port("destination"))

      res.toNsiEnvelope() must \\("sourceSTP") \> "source"
      res.toNsiEnvelope() must \\("destSTP") \> "destination"
    }

    "have a soap action" in {
      val res = DefaultReservation()

      res.soapAction must equalTo("http://schemas.ogf.org/nsi/2013/12/connection/service/reserve")
    }

    "carry an oauth token in the soap headers" in {
      val res = DefaultReservation()
      val token = "foo"
      res.toNsiEnvelope(None, List(token)) must \("Header") \ ("nsiHeader") \
        ("sessionSecurityAttr") \\ ("AttributeValue") \> token
    }

    "not carry an sessionsecurityAttr element in soap headers when no token and no remote user was specified" in {
      val res = DefaultReservation()

      res.toNsiEnvelope() must not \\ ("sessionSecurityAttr")
    }

    "carry an remote user in the soap headers" in {
      val res = DefaultReservation()
      val user = "Chris"
      res.toNsiEnvelope(Some(user)) must \("Header") \ ("nsiHeader") \
        ("sessionSecurityAttr") \\ ("AttributeValue") \> user
    }

  }

  object DefaultReservation {
    def apply(
        description: Option[String] = None,
        start: Option[Date] = Some(new Date()),
        end: Date = new Date(),
        globalReservationId: Option[String] = Some("urn:surfnet:123456")
    ) = {
      val provider = Provider("urn:default-provider", uri("http://localhost"), "urn:ogf:network:")
      Reserve(
        description,
        start,
        end,
        "connection",
        Port("source"),
        Port("dest"),
        Nil,
        10,
        1,
        "FD5C4151-F980-410A-8565-5E8EDCE880F1",
        Some(uri("http://localhost")),
        "requesterNsa",
        provider,
        globalReservationId
      )
    }
  }

}
