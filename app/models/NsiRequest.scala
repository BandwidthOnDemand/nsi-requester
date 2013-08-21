package models

import scala.xml.Node
import scala.xml.NodeSeq
import java.net.URI

abstract class NsiRequest(correlationId: String, replyTo: Option[URI], providerNsa: String) {

  def nsiV1Body: Node
  def nsiV2Body: Node

  def soapAction(version: Int = 1): String = version match {
    case 1 => nsiV1SoapAction
    case 2 => nsiV2SoapAction
    case x => sys.error(s"Non supported NSI version $x")
  }

  private[models] def nsiV1SoapAction: String = ""
  private[models] def nsiV2SoapAction: String = {
    def deCapitalize(input: String): String = {
      val chars = input.toCharArray
      chars(0) = chars(0).toLower
      new String(chars)
    }
    NsiRequest.SoapActionPrefix + deCapitalize(this.getClass().getSimpleName())
  }

  def toNsiEnvelope(version: Int = 1): Node = version match {
    case 1 => toNsiV1Envelope
    case 2 => toNsiV2Envelope
    case x => sys.error(s"Non supported NSI version $x")
  }

  def toNsiV1Envelope: Node = wrapNsiV1Envelope(nsiV1Body)
  def toNsiV2Envelope: Node = wrapNsiV2Envelope(nsiV2Header, nsiV2Body)

  private[models] def nsas = {
    <requesterNSA>{ NsiRequest.RequesterNsa }</requesterNSA>
    <providerNSA>{ providerNsa }</providerNSA>
  }

  private[models] def nsiRequestFields = {
    <int:correlationId>{ "urn:uuid:" + correlationId }</int:correlationId>
    <int:replyTo>{ replyTo.getOrElse(throw new IllegalStateException("replyTo is required for NSIv1")) }</int:replyTo>
  }

  private def nsiV2Header = {
    <head:nsiHeader>
      <protocolVersion>application/vdn.ogf.nsi.cs.v2.provider+soap</protocolVersion>
      <correlationId>{ "urn:uuid:" + correlationId }</correlationId>
      <requesterNSA>{ NsiRequest.RequesterNsa }</requesterNSA>
      <providerNSA>{ providerNsa }</providerNSA>
      { replyTo.fold(NodeSeq.Empty)(replyTo => <replyTo>{ replyTo }</replyTo>) }
    </head:nsiHeader>
  }

  private def wrapNsiV1Envelope(body: Node) = {
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

  private def wrapNsiV2Envelope(header: Node, body: Node) = {
    <soapenv:Envelope
      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:head="http://schemas.ogf.org/nsi/2013/07/framework/headers"
      xmlns:type="http://schemas.ogf.org/nsi/2013/07/connection/types">
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
  val NsiV2ProviderNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/provider"
  val NsiV1ProviderNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider"
  val SoapActionPrefix = "http://schemas.ogf.org/nsi/2013/04/connection/service/"
}