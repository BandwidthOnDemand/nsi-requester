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

import java.net.URI
import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.Period
import scala.xml.NodeSeq.Empty

object Reserve {
  val PathComputationAlgorithms = Seq("Chain", "Sequential", "Tree")
}
case class Reserve(
    description: Option[String],
    startDate: Option[Date],
    endDate: Date,
    serviceType: String,
    source: Port,
    destination: Port,
    ero: List[String],
    bandwidth: Long,
    version: Int = 1,
    correlationId: String,
    replyTo: Option[URI],
    requesterNsa: String,
    provider: Provider,
    globalReservationId: Option[String] = None,
    unprotected: Boolean = false,
    pathComputationAlgorithm: Option[String] = None
) extends NsiRequest(correlationId, replyTo, requesterNsa, provider, addsTrace = true) {

  import NsiRequest._

  override def soapActionSuffix = "reserve"

  override def nsiV2Body =
    <type:reserve>
      { globalReservationIdField }
      { descriptionField }
      <criteria version={ version.toString }>
        <schedule>
          { startTimeField }
          { endTimeField }
        </schedule>
        <serviceType>{ serviceType }</serviceType>
        { service }
      </criteria>
    </type:reserve>

  private def startTimeField = startDate match {
    case Some(date) => <startTime>{ ISODateTimeFormat.dateTime().print(new DateTime(date)) }</startTime>
    case None       => Empty
  }

  private def globalReservationIdField = globalReservationId match {
    case Some(g) => <globalReservationId>{ g }</globalReservationId>
    case None    => <globalReservationId/>
  }

  private def descriptionField = description match {
    case Some(d) => <description>{ d }</description>
    case None    => Empty
  }

  private def endTimeField =
    <endTime>{ ISODateTimeFormat.dateTime().print(new DateTime(endDate)) }</endTime>

  private def eroPresent: Boolean = {
    var found = false;
    ero.withFilter(x => x.nonEmpty).foreach(x => found = true)
    found
  }

  private def service =
    <p2p:p2ps xmlns:p2p={ NsiV2Point2PointNamespace }>
      <capacity>{ bandwidth }</capacity>
      <directionality>Bidirectional</directionality>
      <symmetricPath>true</symmetricPath>
      <sourceSTP>{ source.stpId }</sourceSTP>
      <destSTP>{ destination.stpId }</destSTP>
      {
        if (eroPresent)
      <ero>
        {
          var order = -1;
          for (member <- ero; if member.nonEmpty) yield <orderedSTP order={ order += 1; order.toString }><stp>{ member }</stp></orderedSTP>
        }
      </ero>
      }
      {
        if (unprotected)
          <parameter type="protection">UNPROTECTED</parameter>
        else
          <parameter type="protection">PROTECTED</parameter>
      }
      {
        pathComputationAlgorithm.map(x => <parameter type="pathComputationAlgorithm">{x.toUpperCase}</parameter>).orNull
      }
    </p2p:p2ps>

  private def possibleUnprotected =
    if (unprotected)
      <serviceAttributes>
        <guaranteed>
          <saml:Attribute
            xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
            NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:basic" Name="sNCP">
            <saml:AttributeValue
              xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              Unprotected
            </saml:AttributeValue>
          </saml:Attribute>
        </guaranteed>
      </serviceAttributes>
    else
      Empty
}
