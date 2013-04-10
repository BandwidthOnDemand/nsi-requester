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
import support.JsonResponse.toJson
import scala.xml.NodeSeq

object ResponseController extends Controller {

  private val channels: TMap.View[String, Channel[String]] = TMap().single

  private val CorrelationId = "urn:uuid:(.*)".r

  def reply = Action(parse.xml) { request =>
    val correlationId = parseCorrelationId(request.body)

    correlationId.foreach { id =>
      channels.get(id).map(_.push(stringify(toJson(request.body, DateTime.now()))))
    }

    correlationId.fold(BadRequest)(_ => Ok)
  }

  private def parseCorrelationId(xml: NodeSeq): Option[String] = {
    (xml \\ "correlationId").theSeq.headOption.flatMap { correlationId =>
      correlationId.text match {
        case CorrelationId(id) => Some(id)
        case _ => None
      }
    }
  }

  def comet(id: String) = Action {
    val (enumerator, channel) = Concurrent.broadcast[String]

    channels += (id -> channel)

    Ok.stream(enumerator &> Comet(callback = "parent.message"))
  }

}