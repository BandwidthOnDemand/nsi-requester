package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models._
import RequesterSession._
import support.BuildInfo

object SettingsController extends Controller {

  def settingsForm = Action { implicit request =>
    val defaultForm = settingsF.fill(currentEndPoint)

    Ok(views.html.settings(defaultForm, VersionString))
  }

  def settings = Action { implicit request =>
    settingsF.bindFromRequest.fold[SimpleResult](
      formWithErrors => BadRequest(views.html.settings(formWithErrors, VersionString)),
      {
        case endPoint =>{
          Redirect(routes.Application.reserveForm)
            .flashing("success" -> "Settings changed for this session")
            .withSession(
              AccessTokensSessionField -> endPoint.accessTokens.mkString(","),
              ProviderNsaSessionField -> endPoint.provider.nsaId)
        }
      })
  }

  private lazy val VersionString = s"${BuildInfo.version} (${BuildInfo.gitHeadCommitSha})"

  private[controllers] val settingsF: Form[EndPoint] = Form(
    mapping(
      "provider" -> mapping("id" -> nonEmptyText)(id => findProvider(id).get)(provider => Some(provider.nsaId)),
      "accessTokens" -> list(text)
    )(EndPoint.apply)(EndPoint.unapply)
  )
}