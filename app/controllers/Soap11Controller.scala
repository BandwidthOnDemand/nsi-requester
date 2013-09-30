package controllers

import play.api.http.ContentTypeOf
import play.api.http.MimeTypes
import play.api.http.ContentTypes._

trait Soap11Controller {

  val ContentTypeSoap11 = "text/xml"

  implicit val soapContentType = ContentTypeOf[scala.xml.Node](Some(withCharset(ContentTypeSoap11)))

}