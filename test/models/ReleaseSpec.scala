package models

import org.junit.runner.RunWith

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class ReleaseSpec extends support.Specification with org.specs2.matcher.XmlMatchers {

  "NSI release" should {

    "give valid xml for release that does not contain a connection trace element" in {
      val envelope = DefaultRelease().copy(connectionId = "12345").toNsiEnvelope()

      envelope must \\("release")
      envelope must \\("connectionId") \> "12345"
      envelope must not \\ ("ConnectionTrace")
    }

  }

  object DefaultRelease {
    def apply() = Release(
      "123-abc",
      "asdf-1234",
      Some(uri("http://localhost")),
      "requesterNsa",
      Provider("providerNsa", uri("http://localhost"), "urn:ogf:network:")
    )
  }

}
