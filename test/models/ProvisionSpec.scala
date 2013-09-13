package models

import org.junit.runner.RunWith

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class ProvisionSpec extends support.Specification {

  "NSI v1 provision" should {

    "give valid xml for provision" in {
      val provision = DefaultProvision().copy(connectionId = "1234567890")

      val envelope = provision.toNsiV1Envelope

      envelope must \\("provision")
      envelope must \\("connectionId") \> "1234567890"
    }
  }

  "NSI v2 provision" should {

    "give valid xml for provision" in {
      val provision = DefaultProvision().copy(connectionId = "1234567890")

      val envelope = provision.toNsiV2Envelope

      envelope must \\("provision")
      envelope must \\("connectionId") \> "1234567890"
    }

    "have a soap action" in {
      val res = DefaultProvision()

      res.soapAction(NsiVersion.V2) must equalTo("http://schemas.ogf.org/nsi/2013/07/connection/service/provision")
    }
  }

  object DefaultProvision {
    def apply() = Provision("123-abc", "asdf-098", Some(uri("http://localhost/reply")), "requesterNsa", "providerNsa")
  }

}
