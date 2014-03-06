package controllers

import models._
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.Play.current
import play.api.Play
import java.net.URI
import scala.collection.JavaConversions._
import com.typesafe.config.ConfigObject

object RequesterSession {

  val ProviderNsaSessionField = "nsaId"
  val AccessTokenSessionField = "accessToken"
  val ServiceType = "http://services.ogf.org/nsi/2013/12/descriptions/EVTS.A-GOLE"
  val RequesterNsa = current.configuration.getString("requester.nsi.requesterNsa").getOrElse(sys.error("Requester NSA is not configured (requester.nsi.requesterNsa)"))

  def currentPortPrefix(implicit request: Request[AnyContent]): String = currentProvider.portPrefix

  def currentProvider(implicit request: Request[AnyContent]): Provider =
    request.session.get(ProviderNsaSessionField) flatMap findProvider getOrElse allProviders.head

  def currentEndPoint(implicit request: Request[AnyContent]): EndPoint =
    EndPoint(currentProvider, request.session.get("accessToken"))

  def ReplyToUrl(implicit request: Request[AnyContent]) = URI.create(routes.ResponseController.reply.absoluteURL(isUsingSsl))

  private def isUsingSsl(implicit request: Request[AnyContent]) = request.headers.get("X-Forwarded-Proto") == Some("https")

  // is not a lazy val, because some tests will break (object will only be initialized once during tests
  def allProviders: Seq[Provider] = {
    def toProvider(providerObject: ConfigObject): Provider =
      Provider(
        providerObject.get("id").unwrapped().asInstanceOf[String],
        URI.create(providerObject.get("url").unwrapped().asInstanceOf[String]),
        providerObject.get("portPrefix").unwrapped().asInstanceOf[String],
        providerObject.get("2waytls").unwrapped().asInstanceOf[Boolean])

    current.configuration.getObjectList("requester.nsi.providers").map(_.map(toProvider)).getOrElse(sys.error("No NSI providers where configured (requester.ns.providers)"))
  }

  def findProvider(nsaId: String) = allProviders.find(_.nsaId == nsaId)
}