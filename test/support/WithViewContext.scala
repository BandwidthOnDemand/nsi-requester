package support

import controllers.RequesterSession
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Flash
import play.api.test.Injecting
import play.api.test.WithApplication
import views.Context

abstract class WithViewContext(app: Application = GuiceApplicationBuilder().build())
    extends WithApplication(app)
    with Injecting {
  def this(builder: GuiceApplicationBuilder => GuiceApplicationBuilder) = {
    this(builder(GuiceApplicationBuilder()).build())
  }

  implicit val flash: Flash = Flash()
  implicit def viewContext: Context =
    new Context()(app.configuration, app.environment, flash, inject[Messages])
  implicit def requesterSession: RequesterSession = inject[RequesterSession]
}
