package models

import scala.xml.Elem

object SoapHelper {

  def inEnveloppe(xml: Elem) = {
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

  def nsas = {
    <requesterNSA>urn:nl:surfnet:requester:example</requesterNSA>
    <providerNSA>urn:ogf:network:nsa:netherlight</providerNSA>
  }
}