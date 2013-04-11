package models

import org.specs2.mutable.Specification
import org.junit.runner.RunWith

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class ReleaseSpec extends Specification {

  "NSI v1 terminate" should {

    "give valid xml for release" in {
      val envelope = DefaultRelease().copy(connectionId = "12345").toNsiV1Envelope

      envelope must \\("release")
      envelope must \\("connectionId") \> "12345"
    }
  }

  "NSI v2 terminate" should {

    "give valid xml for release" in {
      val envelope = DefaultRelease().copy(connectionId = "12345").toNsiV2Envelope

      envelope must \\("release")
      envelope must \\("connectionId") \> "12345"
    }

  }

  object DefaultRelease {
    def apply() = Release("123-abc", "asdf-1234", "http://localhost", NsiRequest.RequesterNsa)
  }

}