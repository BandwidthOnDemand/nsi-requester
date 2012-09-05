package controllers

import adapters.Pusher
import play.api.mvc.Action
import play.api.mvc.Controller
import support.PrettyXml.nodeSeqToString

object ResponseController extends Controller {

  def reply = Action { request =>
    import support.PrettyXml._

    val soapResponse = request.body.asXml.get.prettify

    Pusher.sendNsiResponse(soapResponse)

    Ok
  }

  def response = Action { implicit request =>
    Ok(views.html.response(None, None))
  }
}
