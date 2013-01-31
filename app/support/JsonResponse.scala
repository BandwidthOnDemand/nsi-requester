package support

import scala.xml.NodeSeq

import org.joda.time.DateTime

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import support.PrettyXml.nodeSeqToString

object JsonResponse {

  def toJson(request: NodeSeq, requestTime: DateTime, response: NodeSeq, responseTime: DateTime): JsValue =
    Json.obj(
      "request" -> jsonObject(request, requestTime),
      "response" -> jsonObject(response, responseTime)
    )

  def toJson(response: NodeSeq, time: DateTime): JsValue =
    Json.obj(
      "response" -> jsonObject(response, time)
    )

  private def jsonObject(response: NodeSeq, time: DateTime): JsValue = {
    import PrettyXml._

    Json.obj(
      "xml" -> response.prettify,
      "time" -> time.toString("HH:mm:ss,SSS")
    )
  }
}