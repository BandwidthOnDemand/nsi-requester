package models

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

case class Reservation(
    description: String,
    startDate: Date,
    endDate: Date,
    connectionId: String,
    correlationId: String,
    source: String = "",
    destination: String = "",
    bandwidth: Int) extends Soapable {

  def toEnvelope(replyTo: String) = {

    val dateTimeFormat = ISODateTimeFormat.dateTime()

    inEnvelope(
      <int:reserveRequest>
        <int:correlationId>{ correlationId }</int:correlationId>
        <int:replyTo>{ replyTo }</int:replyTo>
        <type:reserve>
          { nsas }
          <reservation>
            <connectionId>{ connectionId }</connectionId>
            <description>{ description }</description>
            <serviceParameters>
              <schedule>
                <startTime>{ dateTimeFormat.print(new DateTime(startDate)) }</startTime>
                <endTime>{ dateTimeFormat.print(new DateTime(endDate)) }</endTime>
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

}
