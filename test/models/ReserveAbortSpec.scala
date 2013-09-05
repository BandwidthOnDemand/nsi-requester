package models

import org.junit.runner.RunWith

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class ReserveAbortSpec extends support.Specification {

  "NSI v2 reserveAbort" should {

    "have a SOAP action" in {
      val res = DefaultReserveAbort()

      res.soapAction(NsiVersion.V2) must equalTo("http://schemas.ogf.org/nsi/2013/07/connection/service/reserveAbort")
    }
  }

  object DefaultReserveAbort {
    def apply() = ReserveAbort("connectionId", "correlationId", Some(uri("http://localhost/reply")), "requesterNsa", "providerNsa")
  }
}