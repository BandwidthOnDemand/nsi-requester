package models

import org.specs2.mutable.Specification
import org.junit.runner.RunWith

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class ProvisionSpec extends Specification {

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
  }

  object DefaultProvision {
    def apply() = Provision("123-abc", "asdf-098", "http://localhost/reply", NsiRequest.RequesterNsa)
  }

}