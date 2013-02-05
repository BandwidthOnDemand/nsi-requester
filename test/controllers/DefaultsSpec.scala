package controllers

import org.specs2.mutable.Specification
import play.api.test.FakeRequest
import play.api.test.WithApplication
import play.api.test.WithApplication
import play.api.test.WithApplication

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class DefaultsSpec extends Specification {

  "The default provider" should {

    "contain password and username from session" in new WithApplication {

      val provider = Defaults.defaultProvider(FakeRequest().withSession("password" -> "secret", "username" -> "John"))

      provider.password must beSome("secret")
      provider.username must beSome("John")
    }

    "have some defaults without settings set" in {

      val provider = Defaults.defaultProvider(FakeRequest())

      provider.providerUrl must beEqualTo("https://bod.surfnet.nl/nsi/v1_sc/provider")
      provider.accessToken must beNone
      provider.password must beNone
      provider.username must beNone
    }
  }

  "The default providerNsa" should {

    "be filled without a session" in {
      val providerNsa = Defaults.defaultProviderNsa(FakeRequest())

      providerNsa must beEqualTo("urn:ogf:network:nsa:surfnet.nl")
    }

    "be filled from the session" in new WithApplication {
      val providerNsa = Defaults.defaultProviderNsa(FakeRequest().withSession("providerNsa" -> "urn:ogf:network:nsa:some-network"))

      providerNsa must beEqualTo("urn:ogf:network:nsa:some-network")
    }
  }
}