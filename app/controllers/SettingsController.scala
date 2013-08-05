package controllers

import java.net.URI
import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.data.Form
import play.api.data.Forms._
import models.Provider
import Defaults._
import FormSupport._

object SettingsController extends Controller {

  def settingsForm = Action { implicit request =>
    val defaultForm = settingsF.fill((defaultProvider, (defaultReplyToUrl, defaultProviderNsa)))

    Ok(views.html.settings(defaultForm))
  }

  def settings = Action { implicit request =>
    settingsF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.settings(formWithErrors)),
      {
        case (provider, (replyTo, providerNsa)) =>
          Redirect(routes.Application.reserveForm).flashing("success" -> "Settings changed for this session")
            .withSession(
              "providerUrl" -> provider.providerUrl.toString,
              "nsiVersion" -> provider.nsiVersion.toString,
              "username" -> provider.username.getOrElse(""),
              "password" -> provider.password.getOrElse(""),
              "accessToken" -> provider.accessToken.getOrElse(""),
              "replyTo" -> replyTo.map(_.toString).getOrElse(""),
              "providerNsa" -> providerNsa)
      })
  }

  def resetSettings = Action { implicit request =>
    Redirect(routes.SettingsController.settingsForm).withNewSession.flashing("success" -> "Settings have been reset")
  }

  private[controllers] val settingsF: Form[(Provider, (Option[URI], String))] = Form(
    tuple(
      "provider" -> providerMapping,
      "nsi" -> tuple(
        "replyTo" -> optional(uri),
        "providerNsa" -> nonEmptyText)))
}
