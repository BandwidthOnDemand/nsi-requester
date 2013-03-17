package support

import scala.xml.NodeSeq
import org.joda.time.DateTime
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import support.PrettyXml.nodeSeqToString
import play.api.libs.json.JsObject
import java.util.UUID

object JsonResponse {

  def toJson(request: NodeSeq, requestTime: DateTime, response: NodeSeq, responseTime: DateTime): JsObject =
    Json.obj(
      "request" -> jsonObject(request.prettify, requestTime),
      "response" -> jsonObject(response.prettify, responseTime)
    )

  def toJson(response: NodeSeq, time: DateTime): List[JsObject] = {
    val data = response.prettify
    val id = UUID.randomUUID().toString()

    data.getBytes("UTF-8").grouped(9500).map { message =>
      Json.obj("id" -> id, "response" -> jsonObject(new String(message, "UTF-8"), time))
    }.toList
  }

  private def jsonObject(data: String, time: DateTime): JsValue = {
    Json.obj(
      "xml" -> data,
      "time" -> time.toString("HH:mm:ss,SSS")
    )
  }
}