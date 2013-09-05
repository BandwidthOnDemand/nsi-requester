package models

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.Period

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class ReserveSpec extends support.Specification with org.specs2.matcher.XmlMatchers {

  "reserves" should {

    "have an envelope without startTime" in {
      val res = DefaultReservation(start = None)

      res.toNsiV1Envelope must not \\("startTime")
    }

    "have an envelope with endTime" in {
      val endDate = new DateTime(2015, 1, 1, 12, 10).toDate
      val res = DefaultReservation(end = Left(endDate))

      res.toNsiV1Envelope must \\("endTime") \> "2015-01-01T12:10:00.000+01:00"
    }

    "have an envelope with duration" in {
      val res = DefaultReservation(end = Right(new Period(2, 10, 0, 0)))

      res.toNsiV1Envelope must \\("duration") \> "PT2H10M"
    }

    "have an envelope with a description" in {
      val res = DefaultReservation(description = Some("My new reservation"))

      res.toNsiV1Envelope must \\("description") \> "My new reservation"
    }

    "have an envelope without a description" in {
      val res = DefaultReservation(description = None)

      res.toNsiV1Envelope must not \\ ("description")
    }

    "have an envelope with a globalReservationId" in {
      val res = DefaultReservation(globalReservationId = Some("urn:surfnet:123456"))

      res.toNsiV1Envelope must \\("globalReservationId") \> "urn:surfnet:123456"
    }

    "have an envelope with an empty globalReservationid" in {
      val res = DefaultReservation(globalReservationId = None)

      res.toNsiV1Envelope must \\("globalReservationId") \> ""
    }
  }

  "NSI 2 reserve" should {

    "have a NSI header element" in {
      val res = DefaultReservation().copy(correlationId = "FD5C4151-F980-410A-8565-5E8EDCE880F1")

      res.toNsiV2Envelope must \("Header") \("nsiHeader") \("correlationId") \> "urn:uuid:FD5C4151-F980-410A-8565-5E8EDCE880F1"
    }

    "have a STP id split up by network and local id" in {
      val res = DefaultReservation().copy(source = Port("urn:ogf:network:stp:surfnet.nl", "22"))

      res.toNsiV2Envelope must \\("sourceSTP") \("networkId") \> "urn:ogf:network:stp:surfnet.nl"
      res.toNsiV2Envelope must \\("sourceSTP") \("localId") \> "22"
    }

    "have a source and destination STP id" in {
      val res = DefaultReservation().copy(source = Port("source", "port"), destination = Port("destination", "port"))

      res.toNsiV2Envelope must \\("sourceSTP") \("networkId") \> "source"
      res.toNsiV2Envelope must \\("destSTP") \("networkId") \> "destination"
    }

    "have a soap action" in {
      val res = DefaultReservation()

      res.soapAction(NsiVersion.V2) must equalTo("http://schemas.ogf.org/nsi/2013/07/connection/service/reserve")
    }
  }

  object DefaultReservation {
    def apply(description: Option[String] = None, start: Option[Date] = Some(new Date()), end: Either[Date, Period] = Left(new Date()), globalReservationId: Option[String] = Some("urn:surfnet:123456")) =
      Reserve(description, start, end, "connection", "", Port("source", "1"), Port("dest", "2"), 10, "FD5C4151-F980-410A-8565-5E8EDCE880F1", Some(uri("http://localhost")), "nsa:surfnet.nl", globalReservationId)
  }

}
