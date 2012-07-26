package models

import org.specs2.mutable.Specification

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
object QuerySpec extends Specification {

  "queries" should {

    "contain an envelope with connectionIds" in {
      val query = Query(connectionIds = List("1", "3"), globalReservationIds = Nil, correlationId = "correlation", replyTo = "http://localhost", nsaProvider = "nsa:surfnet.nl")
      val envelope = query.toEnvelope

      envelope must \\("connectionId") \> "1"
      envelope must \\("connectionId") \> "3"
      envelope must not \\("globalReservationId")
    }

    "contain an envelope with glogalIds" in {
      val query = Query(connectionIds = Nil, globalReservationIds = List("1", "2"), correlationId = "corr", replyTo = "http://localhost", nsaProvider = "nsa:surfnet.nl")
      val envelope = query.toEnvelope

      envelope must \\("globalReservationId") \> "1"
      envelope must \\("globalReservationId") \> "2"
      envelope must not \\("connectionId")
    }

    "contain an envelope with both globalReservationIds and ConnecionIds" in {
      val query = Query(connectionIds = List("5", "9"), globalReservationIds = List("1", "2"), correlationId = "corr", replyTo = "http://localhost", nsaProvider = "nsa:surfnet.nl")
      val envelope = query.toEnvelope

      envelope must \\("globalReservationId") \> "2"
      envelope must \\("connectionId") \> "9"
    }

    "doen't contain any filter" in {
      val query = Query(connectionIds = Nil, globalReservationIds = Nil, correlationId = "corr", replyTo = "http://localhost", nsaProvider = "nsa:surfnet.nl")
      val envelope = query.toEnvelope

      envelope must \\("queryFilter")
      envelope must not \\("connectionId")
      envelope must not \\("globalReservationId")
    }
  }
}
