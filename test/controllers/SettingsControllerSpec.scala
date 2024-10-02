package controllers

import models._
import play.api.test._
import support.WithViewContext

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class SettingsControllerSpec extends support.Specification {

  "The SettingsController" should new WithApplication with Injecting {

    val subject = inject[SettingsController]

    val basicSettings = Seq(
      "provider.id" -> "urn:ogf:network:surfnet.nl:1990:nsa:bod-dev"
    )

    "store settings in the session" in {

      val result = subject.settings()(FakeRequest().withFormUrlEncodedBody(basicSettings: _*))

      status(result) must equalTo(303)

      session(result).get(RequesterSession.ProviderNsaSessionField) must beSome(
        "urn:ogf:network:surfnet.nl:1990:nsa:bod-dev"
      )
      flash(result).get("success") must beSome
    }

    "store the access token in session" in {

      val data = basicSettings ++ Seq(
        "accessTokens[0]" -> "secretToken"
      )

      val result = subject.settings()(FakeRequest().withFormUrlEncodedBody(data: _*))

      session(result).get(RequesterSession.AccessTokensSessionField) must beSome("secretToken")
    }

  }

  "The settings view" should new WithViewContext {
    val subject = inject[SettingsController]

    "contain the stored tokens that were previously set in session" in {
      val endpoint = EndPoint(
        Provider("urn:provider", uri("http://localhost"), "urn:ogf:network:"),
        List("token1")
      )
      val settingsForm = subject.settingsF.fill(endpoint)

      val result = views.html.settings(settingsForm, "version")

      contentAsString(result) must contain("""name="accessTokens[0]" value="token1"""")
    }
  }
}
