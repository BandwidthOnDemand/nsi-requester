package models

import java.net.URI
import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.Period
import scala.xml.NodeSeq.Empty

case class Reserve(
    description: Option[String],
    startDate: Option[Date],
    endDate: Date,
    serviceType: String,
    source: Port,
    destination: Port,
    bandwidth: Long,
    correlationId: String,
    replyTo: Option[URI],
    requesterNsa: String,
    provider: Provider,
    globalReservationId: Option[String] = None,
    unprotected: Boolean = false) extends NsiRequest(correlationId, replyTo, requesterNsa, provider) {

  import NsiRequest._

  override def nsiV2Body =
    <type:reserve>
      { globalReservationIdField }
      { descriptionField }
      <criteria version="1">
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

  private def service =
    <p2p:p2ps xmlns:p2p={ NsiV2Point2PointNamespace }>
      <capacity>{ bandwidth }</capacity>
      <directionality>Bidirectional</directionality>
      <symmetricPath>true</symmetricPath>
      <sourceSTP>{ source.stpId }</sourceSTP>
      <destSTP>{ destination.stpId }</destSTP>
      {
        if (unprotected)
          <parameter type="protection">UNPROTECTED</parameter>
        else
          <parameter type="protection">PROTECTED</parameter>
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
