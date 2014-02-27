package controllers

import models._
import play.api.mvc.Request
import play.api.mvc.AnyContent
import scala.util.Try
import java.net.URI

object Defaults {

  val DefaultPortPrefix = Port("urn:ogf:network:surfnet.nl:1990:testbed:")
  val DefaultProvider = Provider.all.head
  val DefaultServiceType = "http://services.ogf.org/nsi/2013/12/descriptions/EVTS.A-GOLE"
  val ProviderNsa = "urn:ogf:network:surfnet.nl:1990:nsa:bod"
  val RequesterNsa = "urn:ogf:network:nsa:surfnet-nsi-requester"

  def currentEndPoint(implicit request: Request[AnyContent]) = {
    val provider = request.session.get("nsaId").flatMap(id => Provider.find(id)).getOrElse(DefaultProvider)
    val token = request.session.get("accessToken")

    EndPoint(provider, token)
  }

  def ReplyToUrl(implicit request: Request[AnyContent]) = URI.create("http://" + request.host + routes.ResponseController.reply)

}
