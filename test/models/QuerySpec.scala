package models

import org.specs2.mutable.Specification

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class QuerySpec extends Specification {

  "queries" should {

    "contain an envelope with connectionIds" in {
      val query = defaultQuery(connectionIds = List("1", "3"))
      val envelope = query.toNsiV1Envelope

      envelope must \\("connectionId") \> "1"
      envelope must \\("connectionId") \> "3"
      envelope must not \\("globalReservationId")
    }

    "contain an envelope with glogalIds" in {
      val query = defaultQuery(globalReservationIds = List("1", "2"))
      val envelope = query.toNsiV1Envelope

      envelope must \\("globalReservationId") \> "1"
      envelope must \\("globalReservationId") \> "2"
      envelope must not \\("connectionId")
    }

    "contain an envelope with both globalReservationIds and ConnecionIds" in {
      val query = defaultQuery(connectionIds = List("5", "9"), globalReservationIds = List("1", "2"))
      val envelope = query.toNsiV1Envelope

      envelope must \\("globalReservationId") \> "2"
      envelope must \\("connectionId") \> "9"
    }

    "doen't contain any filter" in {
      val query = defaultQuery(connectionIds = Nil, globalReservationIds = Nil)
      val envelope = query.toNsiV1Envelope

      envelope must \\("queryFilter")
      envelope must not \\("connectionId")
      envelope must not \\("globalReservationId")
    }
  }

  object defaultQuery {
    def apply(connectionIds: List[String] = Nil, globalReservationIds: List[String] = Nil) =
        Query(connectionIds = connectionIds, globalReservationIds = globalReservationIds, correlationId = "corr", replyTo = "http://localhost", nsaProvider = "nsa:surfnet.nl")
  }
}
