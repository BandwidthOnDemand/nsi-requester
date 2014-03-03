package models

import QueryOperation._

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class QuerySpec extends support.Specification with org.specs2.matcher.XmlMatchers {

  "NSI queries" should {

    "give a querySummary containing a connectionId" in {
      val query = DefaultQuery(connectionIds = List("abc-123"))
      val envelope = query.toNsiEnvelope

      envelope must \\("querySummary")
      envelope must \\("connectionId") \> "abc-123"
      envelope must not \\("globalReservationId")
    }

   "give a querySummarySync containing a connectionId" in {
      val query = DefaultQuery(connectionIds = List("abc-123"), operation = SummarySync)
      val envelope = query.toNsiEnvelope

      envelope must \\("querySummarySync")
      envelope must \\("connectionId") \> "abc-123"
    }

   "give a queryRecursive containing a connectionId" in {
      val query = DefaultQuery(connectionIds = List("abc-123"), operation = Recursive)
      val envelope = query.toNsiEnvelope

      envelope must \\("queryRecursive")
      envelope must \\("connectionId") \> "abc-123"
    }

   "give an exception for a not supported NSI 2 operation" in {
      val query = DefaultQuery(operation = Details)

      query.toNsiEnvelope must throwA[RuntimeException]("Unsupported NSI v2 query type 'Details'")
    }

    "have a soap action" in {
      val query = DefaultQuery(operation = Recursive)

      query.soapAction() must equalTo("http://schemas.ogf.org/nsi/2013/12/connection/service/queryRecursive")
    }
  }

  object DefaultQuery {

    def apply(connectionIds: List[String] = Nil, globalReservationIds: List[String] = Nil, operation: QueryOperation = Summary) =
      Query(
        connectionIds = connectionIds,
        globalReservationIds = globalReservationIds,
        correlationId = "corr",
        operation = operation,
        replyTo = Some(uri("http://localhost")),
        requesterNsa = "requesterNsa",
        provider = Provider("nsa:surfnet.nl", uri("http://localhost")))
  }
}
