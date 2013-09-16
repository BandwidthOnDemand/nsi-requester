package controllers

import models._
import play.api.mvc.Request
import play.api.mvc.AnyContent
import scala.util.Try
import java.net.URI

object Defaults {

  val DefaultPortV1 = Port("", "urn:ogf:network:stp:surfnet.nl:", None)
  val DefaultPortV2 = Port("urn:ogf:network:surfnet.nl:1990:topology:surfnet6:testbed", "urn:ogf:network:surfnet.nl:1990:port:surfnet6:testbed:", None)

  private val DefaultProviderUrl = URI.create("https://bod.surfnet.nl/nsi/v1_sc/provider")
  val DefaultProviderNsa: Map[NsiVersion, String] = Map(NsiVersion.V1 -> "urn:ogf:network:nsa:surfnet.nl", NsiVersion.V2 -> "urn:ogf:network:surfnet.nl:1990:nsa:bod")
  val DefaultRequesterNsa = "urn:ogf:network:nsa:surfnet-nsi-requester"
  private val DefaultNsiVersion = NsiVersion.V2

  def defaultProvider(implicit request: Request[AnyContent]) = {
    val url = request.session.get("providerUrl").flatMap(s => Try(URI.create(s)).toOption).getOrElse(DefaultProviderUrl)
    val nsiVersion = defaultNsiVersion
    val user = request.session.get("username")
    val pass = request.session.get("password")
    val token = request.session.get("accessToken")

    Provider(url, nsiVersion, user, pass, token)
  }

  def defaultNsiVersion(implicit request: Request[AnyContent]) =
    request.session.get("nsiVersion").map(s => NsiVersion.fromInt(s.toInt)).getOrElse(DefaultNsiVersion)

  def defaultProviderNsa(implicit request: Request[AnyContent]) =
    request.session.get("providerNsa").getOrElse(DefaultProviderNsa(defaultNsiVersion))

  def defaultRequesterNsa(implicit request: Request[AnyContent]) =
    request.session.get("requesterNsa").getOrElse(DefaultRequesterNsa)

  def defaultReplyToUrl(implicit request: Request[AnyContent]) =
    request.session.get("replyTo").filter(_.nonEmpty).orElse(Some("http://" + request.host + routes.ResponseController.reply)).flatMap(s => Try(URI.create(s)).toOption)

}
