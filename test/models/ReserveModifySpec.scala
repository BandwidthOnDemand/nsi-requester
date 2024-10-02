package models

import java.util.Date

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class ReserveModifySpec extends support.Specification with org.specs2.matcher.XmlMatchers {

  "NSI reserve modify" should {

    "have a NSI header element with a connection trace" in {
      val res = DefaultModify().copy(correlationId = "FD5C4151-F980-410A-8565-5E8EDCE880F1")

      val envelope = res.toNsiEnvelope()
      envelope must \("Header") \ ("nsiHeader") \ ("correlationId") \>
        "urn:uuid:FD5C4151-F980-410A-8565-5E8EDCE880F1"
      envelope must \("Header") \ ("nsiHeader") \ ("ConnectionTrace") \ ("Connection")
    }

    "not have a source and destination STP id" in {
      val res = DefaultModify()

      res.toNsiEnvelope() must not \\ ("sourceSTP")
      res.toNsiEnvelope() must not \\ ("destSTP")
    }

    "have a soap action" in {
      val res = DefaultModify()

      res.soapAction must equalTo("http://schemas.ogf.org/nsi/2013/12/connection/service/reserve")
    }
  }

  def DefaultModify(
      connectionId: String = "1234",
      start: Option[Date] = None,
      end: Option[Date] = None,
      bandwidth: Option[Long] = None,
      version: Int = 1
  ): ReserveModify = {
    val provider = Provider("urn:default-provider", uri("http://localhost"), "urn:ogf:network:")
    ReserveModify(
      connectionId = connectionId,
      startDate = start,
      startNow = false,
      endDate = end,
      indefiniteEnd = false,
      bandwidth = bandwidth,
      version = version,
      correlationId = "FD5C4151-F980-410A-8565-5E8EDCE880F1",
      replyTo = Some(uri("http://localhost")),
      requesterNsa = "requesterNsa",
      provider = provider
    )
  }

}
