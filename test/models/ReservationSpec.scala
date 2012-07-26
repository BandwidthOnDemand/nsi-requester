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
      val res = Reservation(None, new Date, Left(endDate), "con", "source", "dest", 10, "cor", "http://localhost", "nsa:surfnet.nl")

      res.toEnvelope must \\("endTime") \> "2015-01-01T12:10:00.000+01:00"
    }

    "have an envelope with duration" in {
      val res = Reservation(None, new Date, Right(new Period(2, 10, 0, 0)), "con", "source", "dest", 10, "corr", "http://localhost",
        "nsa:surfnet.nl")

      res.toEnvelope must \\("duration") \> "PT2H10M"
    }

    "have an envelope with a description" in {
      val res = Reservation(Some("My new reservation"), new Date, Left(new Date), "con", "source", "dest", 10, "cor", "http://localhost", "nsa:surfnet.nl")

      res.toEnvelope must \\("description") \> "My new reservation"
    }

    "have an envelope without a description" in {
      val res = Reservation(None, new Date, Left(new Date), "con", "source", "dest", 10, "cor", "http://localhost", "nsa:surfnet.nl")

      res.toEnvelope must not \\("description")
    }

    "have an envelope with a globalReservationId" in {
      val res = Reservation(None, new Date, Left(new Date), "con", "source", "dest", 10, "cor", "http://localhost", "nsa:surfnet.nl",
        Some("urn:surfnet:123456"))

      res.toEnvelope must \\("globalReservationId") \> "urn:surfnet:123456"
    }
  }

}
