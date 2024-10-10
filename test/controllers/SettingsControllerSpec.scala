package controllers

import models.*
import play.api.mvc.*
import play.api.test.*
import support.WithViewContext

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class SettingsControllerSpec extends support.Specification:

  "The SettingsController" should {

    val basicSettings = Seq(
      "provider.id" -> "urn:ogf:network:surfnet.nl:1990:nsa:bod-dev"
    )

    "store settings in the session" in new WithViewContext:
      override def running() =
        val subject = inject[SettingsController]

        val result = subject.settings()(FakeRequest().withFormUrlEncodedBody(basicSettings*))

        status(result) must equalTo(303)

        session(result).get(RequesterSession.ProviderNsaSessionField) must beSome(
          "urn:ogf:network:surfnet.nl:1990:nsa:bod-dev"
        )
        flash(result).get("success") must beSome

    "store the access token in session" in new WithViewContext:
      override def running() =
        val subject = inject[SettingsController]

        val data = basicSettings ++ Seq(
          "accessTokens[0]" -> "secretToken"
        )

        val result = subject.settings()(FakeRequest().withFormUrlEncodedBody(data*))

        session(result).get(RequesterSession.AccessTokensSessionField) must beSome("secretToken")

  }

  "The settings view" should {
    "contain the stored tokens that were previously set in session" in new WithViewContext:
      override def running() =
        implicit val request: RequestHeader = FakeRequest()
        val subject = inject[SettingsController]

        val endpoint = EndPoint(
          Provider("urn:provider", uri("http://localhost"), "urn:ogf:network:"),
          List("token1")
        )
        val settingsForm = subject.settingsF.fill(endpoint)

        val result = views.html.settings(settingsForm, "version")

        contentAsString(result) must contain("""name="accessTokens[0]" value="token1"""")
  }
end SettingsControllerSpec
