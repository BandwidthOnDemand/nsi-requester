package controllers

import play.api.test.WithApplication
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.data.Form
import models._
import play.api.test.WithApplication
import play.api.mvc.Flash

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class SettingsControllerSpec extends support.Specification {

  "The SettingsController" should {

    val basicSettings = Seq(
      "provider.id" -> "urn:ogf:network:surfnet.nl:1990:nsa:bod-dev"
    )

    "store stettings in the session" in new WithApplication {

      val result = SettingsController.settings()(FakeRequest().withFormUrlEncodedBody(basicSettings: _*))

      status(result) must equalTo(303)

      session(result).get(RequesterSession.ProviderNsaSessionField) must beSome("urn:ogf:network:surfnet.nl:1990:nsa:bod-dev")
      flash(result).get("success") must beSome
    }

    "store the access token in session" in new WithApplication {

      val data = basicSettings ++ Seq(
        "accessToken" -> "secretToken"
      )

      val result = SettingsController.settings()(FakeRequest().withFormUrlEncodedBody(data: _*))

      session(result).get(RequesterSession.AccessTokenSessionField) must beSome("secretToken")
    }

  }

  "The settings view" should {
    "contain the password when set in session" in new WithApplication {
      val settingsForm = SettingsController.settingsF.fill(EndPoint(Provider("urn:provider", uri("http://localhost"), "urn:ogf:network:", false), Some("secrettoken")))

      val result = views.html.settings(settingsForm)(Flash())

      contentAsString(result) must contain("""name="accessToken" value="secrettoken"""")
    }
  }
}