/*
 * Copyright (c) 2012, 2013, 2014, 2015, 2016 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package models

import scala.xml.Node
import scala.xml.NodeSeq
import java.net.URI

abstract class NsiRequest(
    protocolVersion: String = NsiRequest.NsiV2ProviderProtocolVersion,
    addsTrace: Boolean = false
):
  import NsiRequest.*

  def soapActionPrefix: String = NsiV2SoapActionPrefix
  def soapActionSuffix: String

  def correlationId: String
  def replyTo: Option[URI]
  def requesterNsa: String
  def provider: Provider

  def nsiV2Body: Node

  def soapAction: String = s"$soapActionPrefix/$soapActionSuffix"

  def toNsiEnvelope(remoteUser: Option[String] = None, accessTokens: List[String] = Nil): Node =
    wrapNsiV2Envelope(nsiV2Header(remoteUser, accessTokens), nsiV2Body)

  private[models] def nsas =
    <requesterNSA>{requesterNsa}</requesterNSA>
    <providerNSA>{provider.nsaId}</providerNSA>

  private def nsiV2Header(remoteUser: Option[String], accessTokens: List[String]) =
    <head:nsiHeader>
      <protocolVersion>{protocolVersion}</protocolVersion>
      <correlationId>{"urn:uuid:" + correlationId}</correlationId>
      <requesterNSA>{requesterNsa}</requesterNSA>
      <providerNSA>{provider.nsaId}</providerNSA>
      {replyTo.fold(NodeSeq.Empty)(replyTo => <replyTo>{replyTo}</replyTo>)}
      {
        if remoteUser.isEmpty && accessTokens.isEmpty then NodeSeq.Empty
        else
          <sessionSecurityAttr>
            {
              remoteUser.fold(NodeSeq.Empty) { user =>
                <saml:Attribute Name="user">
                  <saml:AttributeValue xsi:type="xs:string">{user}</saml:AttributeValue>
                </saml:Attribute>
              }
            }
            {
              accessTokens.map { token =>
                <saml:Attribute Name="token">
                  <saml:AttributeValue xsi:type="xs:string">{token}</saml:AttributeValue>
                </saml:Attribute>
              }
            }
          </sessionSecurityAttr>
      }
      {
        if addsTrace
        then <gns:ConnectionTrace>
               <Connection index="0">{requesterNsa + ":noId"}</Connection>
             </gns:ConnectionTrace>
        else NodeSeq.Empty
      }
    </head:nsiHeader>

  private def wrapNsiV2Envelope(header: Node, body: Node) =
    <soapenv:Envelope
      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:head={NsiV2FrameworkHeadersNamespace}
      xmlns:type={NsiV2ConnectionTypesNamespace}
      xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
      xmlns:gns="http://nordu.net/namespaces/2013/12/gnsbod"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:xs="http://www.w3.org/2001/XMLSchema">
      <soapenv:Header>
        {header}
      </soapenv:Header>
      <soapenv:Body>
        {body}
      </soapenv:Body>
    </soapenv:Envelope>
end NsiRequest

object NsiRequest:
  val NsiV2ProviderProtocolVersion = "application/vnd.ogf.nsi.cs.v2.provider+soap"
  val NsiV2RequesterProtocolVersion = "application/vnd.ogf.nsi.cs.v2.requester+soap"

  val NsiV2NamespacePrefix = "http://schemas.ogf.org/nsi/2013/12"
  val NsiV2ProviderNamespace: String = s"$NsiV2NamespacePrefix/connection/provider"
  val NsiV2ConnectionTypesNamespace: String = s"$NsiV2NamespacePrefix/connection/types"
  val NsiV2FrameworkHeadersNamespace: String = s"$NsiV2NamespacePrefix/framework/headers"
  val NsiV2Point2PointNamespace: String = s"$NsiV2NamespacePrefix/services/point2point"
  val NsiV2SoapActionPrefix: String = s"$NsiV2NamespacePrefix/connection/service"
