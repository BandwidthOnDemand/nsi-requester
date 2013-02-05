package controllers

import org.specs2.mutable.Specification
import play.api.test.WithApplication
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.data.Form
import models.Provider
import play.api.test.WithApplication
import play.api.mvc.Flash

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class SettingsControllerSpec extends Specification {

  "The SettingsController" should {

    val basicSettings = Seq(
      "provider.providerUrl" -> "http://someurl.nl",
      "nsi.replyTo" -> "http://localhost:9000/reply",
      "nsi.providerNsa" -> "urn:ogf:network:nsa:surfnet.nl"
    )

    "store stettings in the session" in new WithApplication {

      val result = SettingsController.settings()(FakeRequest().withFormUrlEncodedBody(basicSettings: _*))

      session(result).get("providerUrl") must beSome("http://someurl.nl")
      session(result).get("replyTo") must beSome("http://localhost:9000/reply")
      session(result).get("providerNsa") must beSome("urn:ogf:network:nsa:surfnet.nl")

      flash(result).get("success") must beSome
    }

    "store the username and password for basic auth in session" in new WithApplication {

      val data = basicSettings ++ Seq(
        "provider.username" -> "John",
        "provider.password" -> "secret"
      )

      val result = SettingsController.settings()(FakeRequest().withFormUrlEncodedBody(data: _*))

      session(result).get("username") must beSome("John")
      session(result).get("password") must beSome("secret")
    }

    "store the access token in the session" in new WithApplication {

      val data = basicSettings ++ Seq(
        "provider.accessToken" -> "asdfghjkl"
      )

      val result = SettingsController.settings()(FakeRequest().withFormUrlEncodedBody(data: _*))

      session(result).get("accessToken") must beSome("asdfghjkl")
    }

    "reset should clear the settings in the session" in new WithApplication {
      val initialSessionData = Seq(
        "username" -> "John",
        "password" -> "secret",
        "providerNsa" -> "urn:ogf:network:nsa:unknown",
        "replyTo" -> "http://localhost:7070/reply"
      )

      val result = SettingsController.resetSettings()(FakeRequest().withSession(initialSessionData: _*))

      session(result).get("username") must beNone
      session(result).get("password") must beNone
      session(result).get("replyTo") must beNone

      flash(result).get("success") must beSome
    }
  }

  "The settings view" should {

    "contain the password when set in session" in new WithApplication {
      val settingsForm = SettingsController.settingsF.fill(
          Provider("http://localhost", Some("John"), Some("secret"), None), ("http://localhost/reply", "urn:ogf:network:nsa:surnfet.nl")
      )
      val result = views.html.settings(settingsForm)(Flash())

      contentAsString(result) must contain("""name="provider.username" value="John"""")
      contentAsString(result) must contain("""name="provider.password" value="secret"""")
    }
  }
}