/*
 * Copyright (c) 2012, 2013, 2014, 2015, 2016 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package controllers

import models.*
import play.api.Configuration
import play.api.Environment
import play.api.data.Form
import play.api.data.Forms.*
import play.api.mvc.*
import support.BuildInfo

@javax.inject.Singleton
class SettingsController @javax.inject.Inject() (
    val controllerComponents: ControllerComponents,
    val configuration: Configuration,
    val environment: Environment
)(using requesterSession: RequesterSession)
    extends BaseController
    with ViewContextSupport:
  import RequesterSession.*, requesterSession.*

  def settingsForm: Action[AnyContent] = Action { implicit request =>
    val defaultForm = settingsF.fill(currentEndPoint)

    Ok(views.html.settings(defaultForm, VersionString))
  }

  def settings: Action[AnyContent] = Action { implicit request =>
    settingsF
      .bindFromRequest()
      .fold[Result](
        formWithErrors => BadRequest(views.html.settings(formWithErrors, VersionString)),
        { case endPoint =>
          Redirect(routes.ApplicationController.reserveForm)
            .flashing("success" -> "Settings changed for this session")
            .withSession(
              AccessTokensSessionField -> endPoint.accessTokens.mkString(","),
              ProviderNsaSessionField -> endPoint.provider.nsaId
            )
        }
      )
  }

  private lazy val VersionString = s"${BuildInfo.version} (${BuildInfo.gitHeadCommitSha})"

  private[controllers] val settingsF: Form[EndPoint] = Form(
    mapping(
      "provider" -> mapping("id" -> nonEmptyText)(id => findProvider(id).get)(provider =>
        Some(provider.nsaId)
      ),
      "accessTokens" -> list(text)
    )(EndPoint.apply)(ep => Some(Tuple.fromProductTyped(ep)))
  )
end SettingsController
