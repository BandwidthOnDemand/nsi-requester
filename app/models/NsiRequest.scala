package models

import scala.xml.{Node, Elem}

abstract class NsiRequest(correlationId: String, replyTo: String, providerNsa: String) {

  def toNsiV1Envelope: Node
  def toNsiV2Envelope: Node

  def toNsiEnvelope(version: Int = 1): Node = version match {
    case 1 => toNsiV1Envelope
    case 2 => toNsiV2Envelope
    case x => sys.error(s"Non supported NSI version $x")
  }

  protected def nsas = {
    <requesterNSA>{ NsiRequest.RequesterNsa }</requesterNSA>
    <providerNSA>{ providerNsa }</providerNSA>
  }

  protected def nsiRequestFields = {
    <int:correlationId>{ "urn:uuid:" + correlationId }</int:correlationId>
    <int:replyTo>{ replyTo }</int:replyTo>
  }

  protected def nsiV2Header = {
    <head:nsiHeader>
      <protocolVersion>2.0</protocolVersion>
      <correlationId>{ "urn:uuid:" + correlationId }</correlationId>
      <requesterNSA>{  NsiRequest.RequesterNsa }</requesterNSA>
      <providerNSA>{ providerNsa }</providerNSA>
      <replyTo>{ replyTo }</replyTo>
    </head:nsiHeader>
  }

  protected def wrapNsiV1Envelope(body: Elem) = {
    <soapenv:Envelope
      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:type="http://schemas.ogf.org/nsi/2011/10/connection/types"
      xmlns:int="http://schemas.ogf.org/nsi/2011/10/connection/interface">
      <soapenv:Header />
      <soapenv:Body>
        { body }
      </soapenv:Body>
    </soapenv:Envelope>
  }

  protected def wrapNsiV2Envelope(header: Elem, body: Elem) = {
    <soapenv:Envelope
      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:head="http://schemas.ogf.org/nsi/2013/04/framework/headers"
      xmlns:type="http://schemas.ogf.org/nsi/2013/04/connection/types">
      <soapenv:Header>
        { header }
      </soapenv:Header>
      <soapenv:Body>
        { body }
      </soapenv:Body>
    </soapenv:Envelope>
  }

}

object NsiRequest {
  val RequesterNsa = "urn:ogf:network:nsa:surfnet-nsi-requester"
}