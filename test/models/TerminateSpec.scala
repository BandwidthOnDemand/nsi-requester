package models

import org.junit.runner.RunWith

@RunWith(classOf[org.specs2.runner.JUnitRunner])
class TerminateSpec extends support.Specification with org.specs2.matcher.XmlMatchers {

  "NSI terminate" should {

    "give valid xml for provision" in {
      val envelope = DefaultTerminate().copy(connectionId = "12345").toNsiEnvelope

      envelope must \\("terminate")
      envelope must \\("connectionId") \> "12345"
    }

  }

  object DefaultTerminate {
    def apply() = Terminate("123-abc", "asdf-1234", Some(uri("http://localhost")), "requesterNsa", Provider("providerNsa", uri("http://localhost")))
  }

}