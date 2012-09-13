package models

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.Period
import scala.xml.Null

case class Reserve(
    description: Option[String],
    startDate: Option[Date],
    end: Either[Date, Period],
    connectionId: String,
    source: String = "",
    destination: String = "",
    bandwidth: Int,
    correlationId: String,
    replyTo: String,
    providerNsa: String,
    globalReservationId: String = "") extends NsiRequest(correlationId, replyTo, providerNsa) {

  def toEnvelope = {

    inEnvelope(
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
            </serviceParameters>
            <path>
              <directionality>Bidirectional</directionality>
              <sourceSTP>
                <stpId>{ source }</stpId>
              </sourceSTP>
              <destSTP>
                <stpId>{ destination }</stpId>
              </destSTP>
            </path>
          </reservation>
        </type:reserve>
      </int:reserveRequest>
    )
  }

  private def startTimeField = startDate match {
    case Some(date) => <startTime>{ ISODateTimeFormat.dateTime().print(new DateTime(date)) }</startTime>
    case None => Null
  }
  private def globalReservationIdField = globalReservationId match {
    case g: String => <globalReservationId>{ g }</globalReservationId>
    case _ => <globalReservationId/>
  }

  private def descriptionField = description match {
    case Some(d) => <description>{ d }</description>
    case None => Null
  }

  private def endDateOrDuration() = {
    val dateTimeFormat = ISODateTimeFormat.dateTime()

    end.fold(
      date => <endTime>{ dateTimeFormat.print(new DateTime(date)) }</endTime>,
      duration => <duration>{ duration }</duration>)
  }

}
