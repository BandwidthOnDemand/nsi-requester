package models

import org.junit.runner.RunWith

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class TerminateSpec extends support.Specification {

  "NSI v1 terminate" should {

    "give valid xml for provision" in {
      val envelope = DefaultTerminate().copy(connectionId = "12345").toNsiV1Envelope

      envelope must \\("terminate")
      envelope must \\("connectionId") \> "12345"
    }
  }

  "NSI v2 terminate" should {

    "give valid xml for provision" in {
      val envelope = DefaultTerminate().copy(connectionId = "12345").toNsiV2Envelope

      envelope must \\("terminate")
      envelope must \\("connectionId") \> "12345"
    }

  }

  object DefaultTerminate {
    def apply() = Terminate("123-abc", "asdf-1234", Some(uri("http://localhost")), NsiRequest.RequesterNsa)
  }

}
