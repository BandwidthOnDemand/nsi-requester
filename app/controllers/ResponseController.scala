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

object ResponseController extends Controller {

  private val channels: TMap.View[String, Channel[String]] = TMap().single

  private val CorrelationId = "urn:uuid:(.*)".r

  def reply = Action(parse.xml) { request =>
    val correlationId = parseCorrelationId(request.body)
    val providerNsa = parseProviderNsa(request.body)

    correlationId.foreach { id =>
      val clients = channels.get(id).map(Seq(_)).getOrElse {
        Logger.info(s"Could not find correlation id $id, sending reply to all clients")
        channels.values
      }

      clients foreach { client =>
        client.push(stringify(JsonResponse.response(request.body, DateTime.now())))
      }
    }

    correlationId.fold(BadRequest((<badRequest>Could not find a correlationId</badRequest>).asInstanceOf[NodeSeq])) { c =>
      Ok(Ack(c, providerNsa.getOrElse("No provider NSA found")).toNsiV2Envelope)
    }
  }

  private def parseCorrelationId(xml: NodeSeq): Option[String] =
    (xml \\ "correlationId").theSeq.headOption.flatMap { correlationId =>
      correlationId.text match {
        case CorrelationId(id) => Some(id)
        case _ => None
      }
    }

  private def parseProviderNsa(xml: NodeSeq): Option[String] =
    (xml \\ "providerNSA").headOption.map(n => n.text)

  def comet(id: String) = Action {
    val (enumerator, channel) = Concurrent.broadcast[String]

    channels += (id -> channel)

    Ok.stream(enumerator &> Comet(callback = "parent.message"))
  }

}
