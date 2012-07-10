package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import com.ning.http.client.Realm.AuthScheme
import scala.xml.Elem
import java.util.UUID
import play.api.libs.iteratee.Enumerator
import play.api.data.Form
import play.api.data.Forms._
import models.Reservation
import java.util.Date

object Application extends Controller {

  val providerUrl = "http://localhost:8082/bod/nsi/v1_sc/provider"
  val credentials = "nsi" -> "nsi123"

  val form: Form[Reservation] = Form(
      mapping(
          "description" -> text,
          "startDate" -> date,
          "endDate" -> date,
          "connectionId" -> nonEmptyText,
          "source" -> nonEmptyText,
          "destination" -> nonEmptyText
      ){ Reservation.apply } { Reservation.unapply }
  )

  def index = Action {
    Redirect(routes.Application.request)
  }

  def requestForm = Action {
    val defaultForm = form.fill(
        Reservation(description = "test", startDate = new Date, endDate = new Date, connectionId = generateConnectionId)
    )

    Ok(views.html.request(defaultForm))
  }

  def request = Action { implicit request =>
    form.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.request(formWithErrors)),
        reservation => Async {
          val reserveRequest = reserve(reservation)
          println(reserveRequest)
          WS.url(providerUrl)
            .withAuth(credentials._1, credentials._2, AuthScheme.BASIC)
            .post(reserveRequest).map { response =>
              Ok(response.xml)
          }
        }
    )
  }

  def reply = Action {
    Ok
  }

  private def generateConnectionId = "urn:uuid:%s".formatted(UUID.randomUUID.toString)

  private def reserve(reservation: Reservation) = {
    val replyTo = routes.Application.reply
    putInEnveloppe(
      <int:reserveRequest>
        <int:correlationId>urn:uuid:correlationId</int:correlationId>
        <int:replyTo>{ replyTo }</int:replyTo>
        <type:reserve>
          <requesterNSA>urn:nl:surfnet:requester:example</requesterNSA>
          <providerNSA>urn:ogf:network:nsa:netherlight</providerNSA>
          <reservation>
            <connectionId>{ reservation.connectionId }</connectionId>
            <description>{ reservation.description }</description>
            <serviceParameters>
              <schedule>
                <startTime>{ reservation.startDate }</startTime>
                <endTime>{ reservation.endDate }</endTime>
              </schedule>
              <bandwidth>
                <desired>1000</desired>
                <minimum>750</minimum>
                <maximum>1000</maximum>
              </bandwidth>
            </serviceParameters>
            <path>
              <directionality>Bidirectional</directionality>
              <sourceSTP>
                <stpId>{ reservation.source }</stpId>
              </sourceSTP>
              <destSTP>
                <stpId>{ reservation.destination }</stpId>
              </destSTP>
            </path>
          </reservation>
        </type:reserve>
      </int:reserveRequest>
    )
  }

  private def putInEnveloppe(xml: Elem) = {
    <soapenv:Envelope
      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
      xmlns:type="http://schemas.ogf.org/nsi/2011/10/connection/types"
      xmlns:int="http://schemas.ogf.org/nsi/2011/10/connection/interface">
      <soapenv:Body>
        { xml }
      </soapenv:Body>
    </soapenv:Envelope>
  }

}