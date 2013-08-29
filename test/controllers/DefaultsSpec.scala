package controllers

import play.api.test.FakeRequest
import play.api.test.WithApplication
import play.api.test.WithApplication
import play.api.test.WithApplication
import models.NsiVersion

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class DefaultsSpec extends support.Specification {

  "The default provider" should {

    "contain password and username from session" in new WithApplication {

      val provider = Defaults.defaultProvider(FakeRequest().withSession("password" -> "secret", "username" -> "John"))

      provider.password must beSome("secret")
      provider.username must beSome("John")
    }

    "have some defaults without settings set" in {

      val provider = Defaults.defaultProvider(FakeRequest())

      provider.providerUrl must equalTo(uri("https://bod.surfnet.nl/nsi/v1_sc/provider"))
      provider.nsiVersion must equalTo(NsiVersion.V2)
      provider.accessToken must beNone
      provider.password must beNone
      provider.username must beNone
    }
  }

  "The default providerNsa" should {

    "be filled without a session" in {
      val providerNsa = Defaults.defaultProviderNsa(FakeRequest())

      providerNsa must equalTo("urn:ogf:network:nsa:surfnet.nl:1990")
    }

    "be filled from the session" in new WithApplication {
      val providerNsa = Defaults.defaultProviderNsa(FakeRequest().withSession("providerNsa" -> "urn:ogf:network:nsa:some-network"))

      providerNsa must equalTo("urn:ogf:network:nsa:some-network")
    }
  }
}