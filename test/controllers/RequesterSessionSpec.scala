package controllers

import play.api.test._
import models._
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._
import play.api.Play

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class RequesterSessionSpec extends support.Specification {

  val someFakeProviders: Map[String, _] = ConfigFactory.parseString("""
    requester.nsi.providers = [
      { id = "testid", url = "http://localhost:9999", portPrefix = "urn:ogf:network:" },
      { id = "urn:ogf:network:nsa:some-network", url = "http://localhost:8888", portPrefix = "urn:ogf:network" }
    ]""").root().unwrapped().asScala.toMap

  "The configuration" should {

    "have an end point with a default provider" in new WithApplication(FakeApplication(additionalConfiguration = someFakeProviders)) {
      val endPoint = RequesterSession.currentEndPoint(FakeRequest())

      endPoint.provider.providerUrl must equalTo(uri("http://localhost:9999"))
    }

    "have an endpoint for the session settings" in new WithApplication(FakeApplication(additionalConfiguration = someFakeProviders)) {
      val endPoint = RequesterSession.currentEndPoint(FakeRequest().withSession("nsaId" -> "urn:ogf:network:nsa:some-network"))

      endPoint.provider.nsaId must equalTo("urn:ogf:network:nsa:some-network")
      endPoint.provider.providerUrl must equalTo(uri("http://localhost:8888"))
    }

    "load the configured providers" in new WithApplication(FakeApplication(additionalConfiguration = someFakeProviders)) {
      val provider = RequesterSession.findProvider("testid")

      provider must beSome(Provider("testid", uri("http://localhost:9999"), "urn:ogf:network:"))
    }
  }

}