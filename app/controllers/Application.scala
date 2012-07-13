package controllers

import java.util.Date
import java.util.UUID
import scala.annotation.implicitNotFound
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.PrettyPrinter
import com.ning.http.client.Realm.AuthScheme
import models.Reservation
import play.api.data.Form
import play.api.data.Mapping
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
import models.Provision

object Application extends Controller {

  val defaultProviderUrl = "http://localhost:8082/bod/nsi/v1_sc/provider"
  val defaultProvider = Provider(defaultProviderUrl, "nsi", "nsi123", "http://localhost:9000")

  val providerMapping: Mapping[Provider] = mapping(
    "providerUrl" -> nonEmptyText,
    "username" -> text,
    "password" -> text,
    "replyToHost" -> nonEmptyText){ Provider.apply } { Provider.unapply }

  val reserveF: Form[(Provider, Reservation)] = Form(
      tuple(
          "provider" -> providerMapping,
          "reservation" ->
            mapping(
              "description" -> text,
              "startDate" -> date("yyyy-MM-dd HH:mm"),
              "endDate" -> date("yyyy-MM-dd HH:mm"),
              "connectionId" -> nonEmptyText,
              "correlationId" -> nonEmptyText,
              "source" -> nonEmptyText,
              "destination" -> nonEmptyText,
              "bandwidth" -> number(0, 100000)
            ){ Reservation.apply } { Reservation.unapply }
      )
  )

  val provisionF: Form[(Provider, Provision)] = Form(
      tuple(
        "provider" -> providerMapping,
        "provision" ->
          mapping(
            "connectionId" -> nonEmptyText,
            "correlationId" -> nonEmptyText
          ){ Provision.apply }{ Provision.unapply }
      )
  )

  def index = Action {
    Redirect(routes.Application.reserve)
  }

  def reserveForm = Action {
    val defaultForm = reserveF.fill((
            defaultProvider,
            Reservation(
                description = "A NSI reserve test", startDate = new Date, endDate = new Date,
                connectionId = generateConnectionId, correlationId = generateCorrelationId,
                source = "First port", destination = "Second port", bandwidth = 1000)
    ))

    Ok(views.html.reserve(defaultForm))
  }

  def terminateForm = Action {
    Ok(views.html.terminate())
  }

  def reserve = Action { implicit request =>
    reserveF.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.reserve(formWithErrors)),
        {
          case (provider, reservation) => Async {
            val reserveRequest = reservation.toEnvelope(provider.replyToHost + routes.Application.reply)
            WS.url(provider.providerUrl)
              .withAuth(provider.username, provider.password, AuthScheme.BASIC)
              .post(reserveRequest).map { response =>
                Ok(views.html.response(prettify(reserveRequest), prettify(response.xml)))
              }
          }
        }
    )
  }

  def provisionForm = Action {
    val defaultForm = provisionF.fill((
      defaultProvider,
      Provision(connectionId = "", correlationId = generateCorrelationId)
    ))
    Ok(views.html.provision(defaultForm))
  }

  def provision = Action { implicit request =>
    provisionF.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.provision(formWithErrors)),
      {
        case (provider, provision) => Async {
          val provisionRequest = provision.toEnvelope(provider.replyToHost + routes.Application.reply)
          WS.url(provider.providerUrl)
            .withAuth(provider.username, provider.password, AuthScheme.BASIC)
            .post(provisionRequest).map { response =>
              Ok(views.html.response(prettify(provisionRequest), prettify(response.xml)))
            }
        }
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

}
