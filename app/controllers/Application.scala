package controllers

import java.util.Date
import java.util.UUID
import scala.annotation.implicitNotFound
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.PrettyPrinter
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import com.ning.http.client.Realm.AuthScheme
import models.Reservation
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.PushEnumerator
import play.api.libs.ws.WS
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import models.Provider
import play.api.Configuration
import play.api.Play

object Application extends Controller {

  val defaultProviderUrl = "http://localhost:8082/bod/nsi/v1_sc/provider"
  val credentials = "nsi" -> "nsi123"
  val dateTimeFormat = ISODateTimeFormat.dateTime()

  val form: Form[(Provider, Reservation)] = Form(
      tuple(
          "provider" ->
            mapping(
                "providerUrl" -> nonEmptyText,
                "username" -> text,
                "password" -> text,
                "replyToHost" -> nonEmptyText) { Provider.apply } { Provider.unapply},
          "reservation" ->
            mapping(
              "description" -> text,
              "startDate" -> date("yyyy-MM-dd HH:mm"),
              "endDate" -> date("yyyy-MM-dd HH:mm"),
              "connectionId" -> nonEmptyText,
              "correlationId" -> nonEmptyText,
              "source" -> nonEmptyText,
              "destination" -> nonEmptyText
            ){ Reservation.apply } { Reservation.unapply }
      )
  )

  def index = Action {
    Redirect(routes.Application.request)
  }

  def requestForm = Action {
    val defaultForm = form.fill((
            Provider(defaultProviderUrl, credentials._1, credentials._2, "http://localhost:9000"),
            Reservation(
                description = "A NSI reserve test", startDate = new Date, endDate = new Date,
                connectionId = generateConnectionId, correlationId = generateCorrelationId,
                source = "First port", destination = "Second port")
    ))

    Ok(views.html.request(defaultForm))
  }

  def request = Action { implicit request =>
    form.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.request(formWithErrors)),
        {
          case (provider, reservation) => Async {
            val reserveRequest = reserve(reservation, provider.replyToHost)
            WS.url(provider.providerUrl)
              .withAuth(provider.username, provider.password, AuthScheme.BASIC)
              .post(reserveRequest).map { response =>
                Ok(views.html.response(prettify(response.xml)))
          } }
        }
    )
  }

  private def prettify(xml: Node): String = {
    val printer = new PrettyPrinter(80, 4)
    printer.format(xml)
  }

  private def prettify(xml: NodeSeq): String = {
    xml.foldLeft("")((a, b) => a + prettify(b))
  }

  var clients: List[PushEnumerator[String]] = List()

  def reply = Action { request =>
    clients.foreach { client =>
      client.push(prettify(request.body.asXml.get))
    }
    Ok
  }

  def wsRequest = WebSocket.using[String] { request =>
    val in = Iteratee.foreach[String](println).mapDone { _ =>
      println("Disconnected")
    }

    lazy val out: PushEnumerator[String] = Enumerator.imperative[String]()

    clients = out :: clients

    (in, out)
  }

  private def generateConnectionId = "urn:uuid:%s".formatted(UUID.randomUUID.toString)
  private def generateCorrelationId = generateConnectionId

  private def reserve(reservation: Reservation, replyToHost: String) = {
    val replyTo = replyToHost + routes.Application.reply
    putInEnveloppe(
      <int:reserveRequest>
        <int:correlationId>{ reservation.correlationId }</int:correlationId>
        <int:replyTo>{ replyTo }</int:replyTo>
        <type:reserve>
          <requesterNSA>urn:nl:surfnet:requester:example</requesterNSA>
          <providerNSA>urn:ogf:network:nsa:netherlight</providerNSA>
          <reservation>
            <connectionId>{ reservation.connectionId }</connectionId>
            <description>{ reservation.description }</description>
            <serviceParameters>
              <schedule>
                <startTime>{ dateTimeFormat.print(new DateTime(reservation.startDate)) }</startTime>
                <endTime>{ dateTimeFormat.print(new DateTime(reservation.endDate)) }</endTime>
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