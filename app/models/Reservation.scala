package models

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.Period
import scala.xml.Null

case class Reservation(
    description: Option[String],
    startDate: Date,
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

    val dateTimeFormat = ISODateTimeFormat.dateTime()

    inEnvelope(
      <int:reserveRequest>
        { nsiRequestFields }
        <type:reserve>
          { nsas }
          <reservation>
            <globalReservationId>{ globalReservationId }</globalReservationId>
            { descriptionField }
            <connectionId>{ connectionId }</connectionId>
            <serviceParameters>
              <schedule>
                <startTime>{ dateTimeFormat.print(new DateTime(startDate)) }</startTime>
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
