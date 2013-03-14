package adapters

import java.security.MessageDigest
import scala.Array.canBuildFrom
import scala.xml.NodeSeq
import org.joda.time.DateTime
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.mvc.Http.Status.ACCEPTED
import support.JsonResponse
import scala.util.Success
import scala.util.Failure

object Pusher {

  import play.api.Play.current
  import SecurityString._

  lazy val appId = Play.configuration.getString("pusher.appId").getOrElse(throw new RuntimeException("Missing Pusher app id"))
  lazy val key = Play.configuration.getString("pusher.key").getOrElse(throw new RuntimeException("Missing Pusher key"))
  lazy val secret = Play.configuration.getString("pusher.secret").getOrElse(throw new RuntimeException("Missing Pusher secret key"))

  def sendNsiResponse(response: NodeSeq) = {
    val correlationId = (response \\ "correlationId")

    correlationId foreach (id => {
      val message = JsonResponse.toJson(response, DateTime.now())
      send("response_channel", id.text, Json.stringify(message))
    })
  }

  def send(channel: String, event: String, message: String) = {
    val domain = "api.pusherapp.com"
    val requestPath = s"/apps/$appId/channels/$channel/events"

    val params = List(
        "auth_key" -> key,
        "auth_timestamp" -> (System.currentTimeMillis / 1000).toString,
        "auth_version" -> "1.0",
        "body_md5" -> message.md5(),
        "name" -> event
    ).map{ case (key, value) => "%s=%s".format(key, value) }.mkString("&")

    val signature = List("POST", requestPath, params).mkString("\n").sha256(secret)

    val result = WS.url(s"http://$domain$requestPath?$params&auth_signature=$signature").post(message)

    result onComplete(result => result match {
      case Success(response) => if (response.status != ACCEPTED) println(s"Post to Pusher failed with ${response.status}, ${response.statusText}")
      case Failure(e) => println("Post to Pusher failed with: " + e.getMessage())
    })

    result
  }

}

class SecurityString(input: String) {
  import java.security.MessageDigest
  import javax.crypto.Mac
  import javax.crypto.spec.SecretKeySpec
  import java.math.BigInteger

  implicit def arrayToHexString(data: Array[Byte]) = data.map("%02x".format(_)).mkString

  def md5(): String = MessageDigest.getInstance("MD5").digest(input.getBytes("UTF-8"))

  def sha256(secret: String): String = {
    val algo = "HmacSHA256"
    val mac = Mac.getInstance(algo)
    mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), algo))
    mac.doFinal(input.getBytes)
  }
}

object SecurityString {
  implicit def StringToSecurityString(input: String): SecurityString = new SecurityString(input)
}