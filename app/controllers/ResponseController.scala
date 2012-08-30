package controllers

import play.api.mvc.Controller
import play.api.libs.iteratee.PushEnumerator
import play.api.mvc.WebSocket
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator
import play.api.mvc.Action

object ResponseController extends Controller {

  private var clients: List[PushEnumerator[String]] = List()

  def reply = Action { request =>
    import support.PrettyXml._

    val soapResponse = request.body.asXml.get.prettify
    clients.foreach { client =>
      client.push(soapResponse)
    }
    Ok
  }

  def wsRequest = WebSocket.using[String] { request =>
    val in = Iteratee.foreach[String](println).mapDone { _ =>
      println("Disconnected")
    }

    lazy val out: PushEnumerator[String] = Enumerator.imperative[String]()

    clients = out :: clients

    (in, out)
  }

  def response = Action { implicit request =>
    Ok(views.html.response(None, None))
  }
}
