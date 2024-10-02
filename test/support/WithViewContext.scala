package support

import controllers.RequesterSession
import play.api.Application
import play.api.i18n.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.*
import play.api.test.Injecting
import play.api.test.WithApplication
import views.Context

abstract class WithViewContext(app: Application = GuiceApplicationBuilder().build())
    extends WithApplication(app)
    with Injecting {
  def this(builder: GuiceApplicationBuilder => GuiceApplicationBuilder) = {
    this(builder(GuiceApplicationBuilder()).build())
  }

  implicit def messagesApi: MessagesApi = inject[MessagesApi]

  implicit def viewContext(using request: RequestHeader): Context =
    new Context()(app.configuration, app.environment, request.flash, messagesApi.preferred(request))
  implicit def requesterSession: RequesterSession = inject[RequesterSession]
}
