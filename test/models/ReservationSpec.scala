package models

import org.specs2.mutable.Specification
import java.util.Date
import org.joda.time.DateTime
import org.joda.time.Period

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class ReservationSpec extends Specification {

  "resevations" should {

    "have an envelope with endTime" in {
      val endDate = new DateTime(2015, 1, 1, 12, 10).toDate
      val res = defaultReservation(end = Left(endDate))

      res.toEnvelope must \\("endTime") \> "2015-01-01T12:10:00.000+01:00"
    }

    "have an envelope with duration" in {
      val res = defaultReservation(end = Right(new Period(2, 10, 0, 0)))

      res.toEnvelope must \\("duration") \> "PT2H10M"
    }

    "have an envelope with a description" in {
      val res = defaultReservation(description = Some("My new reservation"))

      res.toEnvelope must \\("description") \> "My new reservation"
    }

    "have an envelope without a description" in {
      val res = defaultReservation(description = None)

      res.toEnvelope must not \\("description")
    }

    "have an envelope with a globalReservationId" in {
      val res = defaultReservation(globalReservationId = Some("urn:surfnet:123456"))

      res.toEnvelope must \\("globalReservationId") \> "urn:surfnet:123456"
    }
  }

  object defaultReservation {
    def apply(description: Option[String] = None, end: Either[Date, Period] = Left(new Date()), globalReservationId: Option[String] = None) =
      Reservation(description, new Date(), end, "connection", "source", "dest", 10, "correlation:1", "http://localhost", "nsa:surfnet.nl", globalReservationId)
  }

}
