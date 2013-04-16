package support

import scala.xml.NodeSeq
import org.joda.time.DateTime
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import support.PrettyXml._
import play.api.libs.json.JsObject
import java.util.UUID

object JsonResponse {

  def success(request: NodeSeq, requestTime: DateTime, response: NodeSeq, responseTime: DateTime): JsObject =
    Json.obj(
      "request" -> jsonObject(request.prettify, requestTime),
      "response" -> jsonObject(response.prettify, responseTime)
    )

  def failure(request: NodeSeq, requestTime: DateTime, message: String): JsObject =
    Json.obj(
      "request" -> jsonObject(request.prettify, requestTime),
      "message" -> message)

  def response(response: NodeSeq, time: DateTime): JsObject =
    Json.obj("response" -> jsonObject(response.prettify, time))

  private def jsonObject(data: String, time: DateTime): JsValue =
    Json.obj(
      "xml" -> data,
      "time" -> time.toString("HH:mm:ss,SSS")
    )
}