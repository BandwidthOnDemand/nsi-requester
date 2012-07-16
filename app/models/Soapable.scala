package models

import scala.xml.Node
import scala.xml.Elem

trait Soapable {
  def toEnvelope(replyTo: String): Node

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

  protected def nsas = {
    <requesterNSA>urn:nl:surfnet:requester:example</requesterNSA>
    <providerNSA>urn:ogf:network:nsa:surfnet.nl</providerNSA>
  }
}
