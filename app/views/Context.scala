package views

import play.api._
import play.api.mvc.Flash
import play.api.i18n.Messages

class Context()(implicit
    val configuration: Configuration,
    val environment: Environment,
    val flash: Flash,
    val messages: Messages
)
