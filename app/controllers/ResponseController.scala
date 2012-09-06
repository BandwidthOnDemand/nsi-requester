package controllers

import adapters.Pusher
import play.api.mvc.Action
import play.api.mvc.Controller

object ResponseController extends Controller {

  def reply = Action { request =>
    import support.PrettyXml._

    val soapResponse = request.body.asXml
    if (soapResponse.isDefined) Pusher.sendNsiResponse(soapResponse.get)

    Ok
  }

  def responses = Action { implicit request =>
    Ok(views.html.response(None, None, None))
  }
}
