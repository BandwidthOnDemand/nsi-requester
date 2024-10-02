package models

import org.junit.runner.RunWith

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class ProvisionSpec extends support.Specification with org.specs2.matcher.XmlMatchers {

  "NSI provision" should {

    "give valid xml for provision" in {
      val provision = DefaultProvision().copy(connectionId = "1234567890")

      val envelope = provision.toNsiEnvelope()

      envelope must \\("provision")
      envelope must \\("connectionId") \> "1234567890"
    }

    "have a soap action" in {
      val res = DefaultProvision()

      res.soapAction must equalTo("http://schemas.ogf.org/nsi/2013/12/connection/service/provision")
    }
  }

  object DefaultProvision {
    def apply(): Provision = Provision(
      "123-abc",
      "asdf-098",
      Some(uri("http://localhost/reply")),
      "requesterNsa",
      Provider("providerNsa", uri("http://localhost"), "urn:ogf:network:")
    )
  }

}
