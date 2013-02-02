package controllers

import models.Provider
import play.api.mvc.Request
import play.api.mvc.AnyContent

object Defaults {

  val defaultStpUriPrefix = "urn:ogf:network:stp:surfnet.nl:"

  private val defaultProviderUrl = "https://bod.surfnet.nl/nsi/v1_sc/provider"
  private val defaultProviderNsaUri = "urn:ogf:network:nsa:surfnet.nl"

  def defaultProvider(implicit request: Request[AnyContent]) = {
    val url = request.session.get("providerUrl").getOrElse(defaultProviderUrl)
    val user = request.session.get("username")
    val pass = request.session.get("password")
    val token = request.session.get("accessToken")

    Provider(url, user, pass, token)
  }

  def defaultProviderNsa(implicit request: Request[AnyContent]) =
    request.session.get("providerNsa").getOrElse(defaultProviderNsaUri)

  def defaultReplyToUrl(implicit request: Request[AnyContent]) =
    request.session.get("replyTo").getOrElse("http://" + request.host + routes.ResponseController.reply)

}