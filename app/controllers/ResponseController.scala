package controllers

import play.api._
import play.api.mvc._
import play.api.libs._
import play.api.libs.json.Json.stringify
import play.api.libs.iteratee._
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.concurrent.Execution.Implicits._
import views.html.defaultpages.badRequest
import org.joda.time.DateTime
import scala.concurrent.stm.TMap
import support.JsonResponse
import scala.xml.NodeSeq
import models.Ack
import models.Provider
import models.NsiRequest

object ResponseController extends Controller with Soap11Controller {

  private val channels: TMap.View[String, Channel[String]] = TMap().single

  private val CorrelationId = "urn:uuid:(.*)".r

  def reply = Action(parse.xml) { request =>
    val correlationId = parseCorrelationId(request.body)
    val providerNsa = parseProviderNsa(request.body)
    val requesterNsa = parseRequesterNsa(request.body)

    correlationId.foreach { id =>
      val clients = channels.get(id).map(Seq(_)).getOrElse {
        Logger.info(s"Could not find correlation id $id, sending reply to all clients")
        channels.values
      }

      clients foreach { client =>
        client.push(stringify(JsonResponse.response(request.body, DateTime.now())))
      }
    }

    correlationId.fold(badRequest("Could not find CorrelationId")) { id =>
      Ok(Ack(id, requesterNsa.getOrElse("not.found.in.request"), providerNsa.flatMap(id => Configuration.findProvider(id)).get).toNsiEnvelope()).as(ContentTypeSoap11)
    }
  }

  private def badRequest(message: String) =
    BadRequest((<badRequest>{ message }</badRequest>).asInstanceOf[NodeSeq])

  private def parseCorrelationId(xml: NodeSeq): Option[String] =
    (xml \\ "correlationId").theSeq.headOption.flatMap { correlationId =>
      correlationId.text match {
        case CorrelationId(id) => Some(id)
        case _ => None
      }
    }

  private def parseRequesterNsa(xml: NodeSeq): Option[String] =
    (xml \\ "requesterNSA").headOption.map(_.text)

  private def parseProviderNsa(xml: NodeSeq): Option[String] =
    (xml \\ "providerNSA").headOption.map(_.text)

  def comet(id: String) = Action {
    val (enumerator, channel) = Concurrent.broadcast[String]

    channels += (id -> channel)

    Ok.chunked(enumerator &> Comet(callback = "parent.message"))
  }

}
