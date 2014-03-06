package controllers

import java.net.URI
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import models._
import RequesterSession._
import FormSupport._

object SettingsController extends Controller {

  def settingsForm = Action { implicit request =>
    val defaultForm = settingsF.fill(currentEndPoint)

    Ok(views.html.settings(defaultForm))
  }

  def settings = Action { implicit request =>
    settingsF.bindFromRequest.fold[SimpleResult](
      formWithErrors => BadRequest(views.html.settings(formWithErrors)),
      {
        case endPoint =>
          Redirect(routes.Application.reserveForm)
            .flashing("success" -> "Settings changed for this session")
            .withSession(
              AccessTokenSessionField -> endPoint.accessToken.getOrElse(""),
              ProviderNsaSessionField -> endPoint.provider.nsaId)
      })
  }

  private[controllers] val settingsF: Form[EndPoint] = Form(
    mapping(
      "provider" -> mapping("id" -> nonEmptyText)(id => findProvider(id).get)(provider => Some(provider.nsaId)),
      "accessToken" -> optional(of[String]))(EndPoint.apply)(EndPoint.unapply)
  )
}