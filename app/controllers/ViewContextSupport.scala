package controllers

import play.api.Configuration
import play.api.Environment
import play.api.i18n.Messages
import play.api.mvc.RequestHeader

trait ViewContextSupport extends play.api.i18n.I18nSupport {
  def configuration: Configuration
  def environment: Environment

  implicit def viewContext(implicit request: RequestHeader): views.Context = new views.Context()(configuration, environment, request.flash, implicitly[Messages])
}
