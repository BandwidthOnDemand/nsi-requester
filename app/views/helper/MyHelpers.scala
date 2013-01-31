package views.helper

import views.html.helper.FieldConstructor
import views.html.fragments.twitterBootstrap2FieldConstructor

object MyHelpers {

  implicit val myFields = FieldConstructor(twitterBootstrap2FieldConstructor.f)

}