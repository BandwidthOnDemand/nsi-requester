package models

import scala.xml.Node
import scala.xml.Elem

trait Soapable {
  def toEnvelope: Node

  protected def inEnvelope(xml: Elem) = {
    <soapenv:Envelope
      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:type="http://schemas.ogf.org/nsi/2011/10/connection/types"
      xmlns:int="http://schemas.ogf.org/nsi/2011/10/connection/interface">
      <soapenv:Header />
      <soapenv:Body>
        { xml }
      </soapenv:Body>
    </soapenv:Envelope>
  }

}
