package controllers

import models._
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.Play.current
import java.net.URI
import scala.collection.JavaConversions._
import com.typesafe.config.ConfigObject

object Configuration {

  val DefaultPortPrefix = "urn:ogf:network:surfnet.nl:1990:testbed:"
  val DefaultProvider = allProviders.head
  val DefaultServiceType = "http://services.ogf.org/nsi/2013/12/descriptions/EVTS.A-GOLE"
  val RequesterNsa = current.configuration.getString("requester.nsi.requesterNsa").getOrElse(sys.error("Requester NSA is not configured (requester.nsi.requesterNsa)"))

  def currentEndPoint(implicit request: Request[AnyContent]) = {
    val provider = request.session.get("nsaId").flatMap(id => findProvider(id)).getOrElse(DefaultProvider)
    val token = request.session.get("accessToken")

    EndPoint(provider, token)
  }

  def ReplyToUrl(implicit request: Request[AnyContent]) = URI.create("http://" + request.host + routes.ResponseController.reply)

  lazy val allProviders: Seq[Provider] = {
    def toProvider(providerObject: ConfigObject): Provider =
      Provider(
        providerObject.get("id").unwrapped().asInstanceOf[String],
        URI.create(providerObject.get("url").unwrapped().asInstanceOf[String]))

    current.configuration.getObjectList("requester.nsi.providers").map(_.map(toProvider)).getOrElse(sys.error("No NSI providers where configured (requester.ns.providers)"))
  }

  def findProvider(nsaId: String) = allProviders.find(_.nsaId == nsaId)
}