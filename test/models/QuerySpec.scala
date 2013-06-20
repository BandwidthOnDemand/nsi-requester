package models

import org.specs2.mutable.Specification
import QueryOperation._

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class QuerySpec extends Specification {

  "NSI v1 queries" should {

    "contain an envelope with connectionIds" in {
      val query = DefaultQuery(connectionIds = List("1", "3"))
      val envelope = query.toNsiV1Envelope

      envelope must \\("connectionId") \> "1"
      envelope must \\("connectionId") \> "3"
      envelope must not \\("globalReservationId")
    }

    "contain an envelope with glogalIds" in {
      val query = DefaultQuery(globalReservationIds = List("1", "2"))
      val envelope = query.toNsiV1Envelope

      envelope must \\("globalReservationId") \> "1"
      envelope must \\("globalReservationId") \> "2"
      envelope must not \\("connectionId")
    }

    "contain an envelope with both globalReservationIds and ConnecionIds" in {
      val query = DefaultQuery(connectionIds = List("5", "9"), globalReservationIds = List("1", "2"))
      val envelope = query.toNsiV1Envelope

      envelope must \\("globalReservationId") \> "2"
      envelope must \\("connectionId") \> "9"
    }

    "doen't contain any filter" in {
      val query = DefaultQuery(connectionIds = Nil, globalReservationIds = Nil)
      val envelope = query.toNsiV1Envelope

      envelope must \\("queryFilter")
      envelope must not \\("connectionId")
      envelope must not \\("globalReservationId")
    }
  }

  "NSI v2 queries" should {

    "give a querySummary containing a connectionId" in {
      val query = DefaultQuery(connectionIds = List("abc-123"))
      val envelope = query.toNsiV2Envelope

      envelope must \\("querySummary")
      envelope must \\("connectionId") \> "abc-123"
      envelope must not \\("globalReservationId")
    }

   "give a querySummarySync containing a connectionId" in {
      val query = DefaultQuery(connectionIds = List("abc-123"), operation = SummarySync)
      val envelope = query.toNsiV2Envelope

      envelope must \\("querySummarySync")
      envelope must \\("connectionId") \> "abc-123"
    }

   "give a queryRecursive containing a connectionId" in {
      val query = DefaultQuery(connectionIds = List("abc-123"), operation = Recursive)
      val envelope = query.toNsiV2Envelope

      envelope must \\("queryRecursive")
      envelope must \\("connectionId") \> "abc-123"
    }

   "give an exception for a not supported NSI 2 operation" in {
      val query = DefaultQuery(operation = Details)

      query.toNsiV2Envelope must throwA[RuntimeException]("Unsupported NSI v2 query type 'Details'")
    }
  }

  object DefaultQuery {

    def apply(connectionIds: List[String] = Nil, globalReservationIds: List[String] = Nil, operation: QueryOperation = Summary) =
      Query(
        connectionIds = connectionIds,
        globalReservationIds = globalReservationIds,
        correlationId = "corr",
        operation = operation,
        replyTo = "http://localhost",
        nsaProvider = "nsa:surfnet.nl")
  }
}
