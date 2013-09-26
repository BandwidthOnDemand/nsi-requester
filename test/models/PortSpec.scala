package models

@org.junit.runner.RunWith(classOf[org.specs2.runner.JUnitRunner])
class PortSpec extends support.Specification with org.specs2.matcher.XmlMatchers {

  "Port" should {
    "contain labels for v2" in {
      val port = Port("networkId", "localId", None, Map("vlan" -> Seq("1", "9")))

      val xml = port.xmlV2

      xml must \\("networkId") \> "networkId"
      xml must \\("labels") \("attribute", "type" -> "vlan")
      xml must \\("attribute") \("value") \> "1"
      xml must \\("attribute") \("value") \> "9"
    }
  }
}