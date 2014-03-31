package models

import scala.xml.Node
import scala.xml.NodeSeq
import java.net.URI

abstract class NsiRequest(correlationId: String, replyTo: Option[URI], requesterNsa: String, provider: Provider, protocolVersion: String = NsiRequest.NsiV2ProviderProtocolVersion) {
  import NsiRequest._

  def nsiV2Body: Node

  def soapAction(): String = {
    def deCapitalize(input: String): String = {
      val chars = input.toCharArray
      chars(0) = chars(0).toLower
      new String(chars)
    }
    s"$NsiV2SoapActionPrefix/${deCapitalize(this.getClass().getSimpleName())}"
  }

  def toNsiEnvelope(remoteUser: Option[String] = None, accessTokens: List[String] = Nil): Node =
    wrapNsiV2Envelope(nsiV2Header(remoteUser, accessTokens, soapAction().endsWith("reserve")), nsiV2Body)


  private[models] def nsas = {
    <requesterNSA>{ requesterNsa }</requesterNSA>
    <providerNSA>{ provider.nsaId }</providerNSA>
  }

  /**
   *
   * @param addTrace trace element is only required for the 'reserve' message
   * @return
   */
  private def nsiV2Header(remoteUser: Option[String], accessTokens: List[String], addTrace: Boolean) =
    <head:nsiHeader>
      <protocolVersion>{ protocolVersion }</protocolVersion>
      <correlationId>{ "urn:uuid:" + correlationId }</correlationId>
      <requesterNSA>{ requesterNsa }</requesterNSA>
      <providerNSA>{ provider.nsaId }</providerNSA>
      { replyTo.fold(NodeSeq.Empty)(replyTo => <replyTo>{ replyTo }</replyTo>) }
      { if (remoteUser.isDefined || !accessTokens.isEmpty) {
          <sessionSecurityAttr>
            { if (remoteUser.isDefined)
                <saml:Attribute Name="user">
                  <saml:AttributeValue xsi:type="xs:string">{ remoteUser.get }</saml:AttributeValue>
                </saml:Attribute>
            }
            { accessTokens.map { token =>
                <saml:Attribute Name="token">
                  <saml:AttributeValue xsi:type="xs:string">{ token }</saml:AttributeValue>
                </saml:Attribute>
              }
            }
          </sessionSecurityAttr>
        }
      }
      { if (addTrace)
          <gns:ConnectionTrace>
            <gns:Connection index="0">
              {requesterNsa + ":noId"}
            </gns:Connection>
          </gns:ConnectionTrace>
      }
    </head:nsiHeader>

  private def wrapNsiV2Envelope(header: Node, body: Node) = {
    <soapenv:Envelope
      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:head={ NsiV2FrameworkHeadersNamespace }
      xmlns:type={ NsiV2ConnectionTypesNamespace }
      xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
      xmlns:gns="http://nordu.net/namespaces/2013/12/gnsbod"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:xs="http://www.w3.org/2001/XMLSchema">
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
  val NsiV2ProviderProtocolVersion = "application/vnd.ogf.nsi.cs.v2.provider+soap"
  val NsiV2RequesterProtocolVersion = "application/vnd.ogf.nsi.cs.v2.requester+soap"

  val NsiV2NamespacePrefix = "http://schemas.ogf.org/nsi/2013/12"
  val NsiV2ProviderNamespace = s"$NsiV2NamespacePrefix/connection/provider"
  val NsiV2ConnectionTypesNamespace = s"$NsiV2NamespacePrefix/connection/types"
  val NsiV2FrameworkHeadersNamespace = s"$NsiV2NamespacePrefix/framework/headers"
  val NsiV2Point2PointNamespace = s"$NsiV2NamespacePrefix/services/point2point"
  val NsiV2SoapActionPrefix = s"$NsiV2NamespacePrefix/connection/service"
}
