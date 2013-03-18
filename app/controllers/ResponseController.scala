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

object ResponseController extends Controller {

  val channels: TMap.View[String, Channel[String]] = TMap().single

  def reply = Action { request =>

    request.body.asXml.map { xml =>
      (xml \\ "correlationId") foreach { id =>
        channels.get(id.text).map(c => c.push(stringify(toJson(xml, DateTime.now()))))
      }
      Ok
    }.getOrElse(BadRequest)

  }

  def comet(id: String) = Action {
    val (enumerator, channel) = Concurrent.broadcast[String]

    channels += (id -> channel)

    Ok.stream(enumerator &> Comet(callback = "parent.message"))
  }

}