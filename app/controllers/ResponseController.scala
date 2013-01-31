package controllers

import adapters.Pusher
import play.api.mvc.Action
import play.api.mvc.Controller
import views.html.defaultpages.badRequest

object ResponseController extends Controller {

  def reply = Action { request =>

    val soapResponse = request.body.asXml

    if (soapResponse.isDefined) {
      Pusher.sendNsiResponse(soapResponse.get)
      Ok
    } else {
      BadRequest
    }

  }

}