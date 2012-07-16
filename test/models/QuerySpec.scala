package models

import org.specs2.mutable.Specification

object QuerySpec extends Specification {

  "queries" should {

    "contain an envelope with connectionIds" in {
      val query = Query("correlation", List("1", "3"), Nil)
      val envelope = query.toEnvelope("http://localhost")

      envelope must \\("connectionId") \> "1"
      envelope must \\("connectionId") \> "3"
      envelope must not \\("globalReservationId")
    }

    "contain an envelope with glogalIds" in {
      val query = Query("asdf", Nil, List("1", "2"))
      val envelope = query.toEnvelope("http://localhost")

      envelope must \\("globalReservationId") \> "1"
      envelope must \\("globalReservationId") \> "2"
      envelope must not \\("connectionId")
    }
  }
}
