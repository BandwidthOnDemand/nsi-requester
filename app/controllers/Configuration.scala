package controllers
import models._
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.Play.current
import scala.util.Try
import java.net.URI

object Configuration {

  val DefaultPortPrefix = "urn:ogf:network:surfnet.nl:1990:testbed:"
  val DefaultProvider = Provider.all.head
  val DefaultServiceType = "http://services.ogf.org/nsi/2013/12/descriptions/EVTS.A-GOLE"
  val RequesterNsa = current.configuration.getString("application.nsi.requesterNsa").getOrElse(sys.error("Requester NSA is not configured (application.nsi.requesterNsa)"))

  def currentEndPoint(implicit request: Request[AnyContent]) = {
    val provider = request.session.get("nsaId").flatMap(id => Provider.find(id)).getOrElse(DefaultProvider)
    val token = request.session.get("accessToken")

    EndPoint(provider, token)
  }

  def ReplyToUrl(implicit request: Request[AnyContent]) = URI.create("http://" + request.host + routes.ResponseController.reply)

}
