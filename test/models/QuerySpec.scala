package models

import org.specs2.mutable.Specification

object QuerySpec extends Specification {

  "queries" should {

    "contain an envelope with connectionIds" in {
      val query = Query(List("1", "3"), Nil, "correlation", "http://localhost", "nsa:surfnet.nl")
      val envelope = query.toEnvelope

      envelope must \\("connectionId") \> "1"
      envelope must \\("connectionId") \> "3"
      envelope must not \\("globalReservationId")
    }

    "contain an envelope with glogalIds" in {
      val query = Query(Nil, List("1", "2"), "corr", "http://localhost", "nsa:surfnet.nl")
      val envelope = query.toEnvelope

      envelope must \\("globalReservationId") \> "1"
      envelope must \\("globalReservationId") \> "2"
      envelope must not \\("connectionId")
    }
  }
}
