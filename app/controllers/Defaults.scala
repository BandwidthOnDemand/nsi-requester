package controllers

import models._
import play.api.mvc.Request
import play.api.mvc.AnyContent

object Defaults {

  val DefaultStpUriPrefix = "urn:ogf:network:stp:surfnet.nl"
  val DefaultPort = Port(DefaultStpUriPrefix, "")

  private val DefaultProviderUrl = "https://bod.surfnet.nl/nsi/v1_sc/provider"
  private val DefaultProviderNsaUri = "urn:ogf:network:nsa:surfnet.nl"
  private val DefaultNsiVersion = 2

  def defaultProvider(implicit request: Request[AnyContent]) = {
    val url = request.session.get("providerUrl").getOrElse(DefaultProviderUrl)
    val nsiVersion = request.session.get("nsiVersion").map(_.toInt).getOrElse(DefaultNsiVersion)
    val user = request.session.get("username")
    val pass = request.session.get("password")
    val token = request.session.get("accessToken")

    Provider(url, nsiVersion, user, pass, token)
  }

  def defaultProviderNsa(implicit request: Request[AnyContent]) =
    request.session.get("providerNsa").getOrElse(DefaultProviderNsaUri)

  def defaultReplyToUrl(implicit request: Request[AnyContent]) =
    request.session.get("replyTo").getOrElse("http://" + request.host + routes.ResponseController.reply)

}