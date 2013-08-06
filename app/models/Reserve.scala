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
    end: Either[Date, Period],
    connectionId: String,
    source: Port,
    destination: Port,
    bandwidth: Long,
    correlationId: String,
    replyTo: Option[URI],
    providerNsa: String,
    globalReservationId: Option[String] = None,
    unprotected: Boolean = false) extends NsiRequest(correlationId, replyTo, providerNsa) {

  override def nsiV1SoapAction = ""
  override def nsiV2SoapAction = "http://schemas.ogf.org/nsi/2013/07/connection/service/reserve"

  override def nsiV2Body =
    <type:reserve>
      { globalReservationIdField }
      { descriptionField }
      <criteria version="0">
        <schedule>
          { startTimeField }
          { endDateOrDuration }
        </schedule>
        <p2p:p2ps xmlns:p2p="http://schemas.ogf.org/nsi/2013/07/services/point2point">
          <capacity>{ bandwidth }</capacity>
          <directionality>Bidirectional</directionality>
          <sourceSTP>
            { source.xmlV2 }
          </sourceSTP>
          <destSTP>
            { destination.xmlV2 }
          </destSTP>
        </p2p:p2ps>
      </criteria>
    </type:reserve>

  override def nsiV1Body =
    <int:reserveRequest>
      { nsiRequestFields }
      <type:reserve>
        { nsas }
        <reservation>
          { globalReservationIdField }
          { descriptionField }
          <connectionId>{ connectionId }</connectionId>
          <serviceParameters>
            <schedule>
              { startTimeField }
              { endDateOrDuration }
            </schedule>
            <bandwidth>
              <desired>{ bandwidth }</desired>
            </bandwidth>
            { possibleUnprotected }
          </serviceParameters>
          <path>
            <directionality>Bidirectional</directionality>
            <sourceSTP>
              <stpId>{ source.stpId }</stpId>
            </sourceSTP>
            <destSTP>
              { destination.xmlV1 }
            </destSTP>
          </path>
        </reservation>
      </type:reserve>
    </int:reserveRequest>

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

  private def endDateOrDuration() = {
    val dateTimeFormat = ISODateTimeFormat.dateTime()

    end.fold(
      date => <endTime>{ dateTimeFormat.print(new DateTime(date)) }</endTime>,
      duration => <duration>{ duration }</duration>)
  }

  private def possibleUnprotected() =
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
