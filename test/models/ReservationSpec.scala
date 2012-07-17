package models

import org.specs2.mutable.Specification
import java.util.Date
import org.joda.time.DateTime
import org.joda.time.Period

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
object ReservationSpec extends Specification {

  "resevations" should {

    "have an envelope with endTime when the endTime is specified" in {
      val endDate = new DateTime(2015, 1, 1, 12, 10).toDate
      val res = Reservation("some", new Date, Left(endDate), "con", "source", "dest", 10, "cor", "http://localhost", "nsa:surfnet.nl")

      res.toEnvelope must \\("endTime") \> "2015-01-01T12:10:00.000+01:00"
    }

    "have an envelope with duration when the duration is specified" in {
      val res = Reservation("some", new Date, Right(new Period(2, 10, 0, 0)), "con", "source", "dest", 10, "corr", "http://localhost",
        "nsa:surfnet.nl")

      res.toEnvelope must \\("duration") \> "PT2H10M"
    }
  }
}
