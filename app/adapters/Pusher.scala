package adapters

import play.api.Play
import play.api.libs.ws.WS
import java.math.BigInteger
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.prefs.Base64

object Pusher {

  import play.api.Play.current
  import SecurityString._

  lazy val appId = Play.configuration.getString("pusher.appId").getOrElse(throw new RuntimeException("Missing Pusher app id"))
  lazy val key = Play.configuration.getString("pusher.key").getOrElse(throw new RuntimeException("Missing Pusher key"))
  lazy val secret = Play.configuration.getString("pusher.secret").getOrElse(throw new RuntimeException("Missing Pusher secret key"))

  def sendNsiResponse(message: String) = {
    send("response_channel", "response", message)
  }

  def send(channel: String, event: String, message: String) = {
    val domain = "api.pusherapp.com"
    val requestPath = "/apps/%s/channels/%s/events".format(appId, channel)

    // socket_id: optional
    val params = List(
        "auth_key" -> key,
        "auth_timestamp" -> (System.currentTimeMillis / 1000).toString,
        "auth_version" -> "1.0",
        "body_md5" -> message.md5(),
        "name" -> event
    ).map{ case (key, value) => "%s=%s".format(key, value) }.mkString("&")

    val signature = List("POST", requestPath, params).mkString("\n").sha256(secret)

    WS.url("http://%s%s?%s&auth_signature=%s".format(domain, requestPath, params, signature)).post(message)
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